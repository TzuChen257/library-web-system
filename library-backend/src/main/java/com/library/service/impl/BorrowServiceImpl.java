package com.library.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.borrow.BorrowResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.BorrowStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import com.library.service.BorrowService;
import com.library.service.NotificationService;
import com.library.util.security.LoginUserHolder;

import lombok.AllArgsConstructor;

@Service
@Transactional//會做transaction因為要修改資料
@AllArgsConstructor
public class BorrowServiceImpl implements BorrowService{

	private static final int MAX_CURRENT_BORROW_COUNT=5;
	private static final int DEFAULT_BORROW_DAYS=14;
	
	private BorrowRecordRepository borrowRecordRepository;
	private BookRepository bookRepository;
	private BookCopyRepository bookCopyRepository;
	private UserRepository userRepository;
	private ReservationRepository reservationRepository;
	private NotificationService notificationService;

	@Override
	public BorrowResponse borrowBook(String bookId) {
		User currentReader=getCurrentReader();
		
		if (Boolean.TRUE.equals(currentReader.getBorrowSuspended())) {
		    throw new LibraryBusinessException(
		            ResponseCode.FORBIDDEN,
		            "您的借書功能已暫停，請聯繫管理員處理逾期書籍與開通借閱權限"
		    );
		}
		
		Book book=bookRepository.findById(bookId)
				.orElseThrow(()->new LibraryBusinessException(ResponseCode.BOOK_NOT_FOUND));
		//確認可以借
		long currentBorrowCount=borrowRecordRepository.countByUser_UserIdAndBorrowStatusIn(
				currentReader.getUserId(),
				Arrays.asList(BorrowStatus.BORROWED,BorrowStatus.RETURN_PENDING,BorrowStatus.OVERDUE));
		
		if(currentBorrowCount>=MAX_CURRENT_BORROW_COUNT) {
			throw new LibraryBusinessException(ResponseCode.BORROW_LIMIT_EXCEEDED);
		}
		//儲存借出(copy)
		BookCopy copy=bookCopyRepository
				.findFirstByBook_BookIdAndCopyStatusOrderByCopyIdAsc(
						book.getBookId(), BookCopyStatus.AVAILABLE)
				.orElseThrow(()->new LibraryBusinessException(ResponseCode.BOOK_COPY_NOT_AVAILABLE));

		copy.setCopyStatus(BookCopyStatus.BORROWED);
		bookCopyRepository.save(copy);
		//儲存借出紀錄
		LocalDate today=LocalDate.now();
		
		BorrowRecord record=new BorrowRecord();
		record.setUser(currentReader);
        record.setBookCopy(copy);
        record.setBorrowDate(today);
        record.setDueDate(today.plusDays(DEFAULT_BORROW_DAYS));
        record.setBorrowStatus(BorrowStatus.BORROWED);
        
        BorrowRecord savedRecord=borrowRecordRepository.save(record);
        
        completeAvailableNoticeReservationIfExists(currentReader.getUserId(), bookId);
        //訊息確認
        notificationService.notifyBorrowSuccess(currentReader, savedRecord);
		
		return toBorrowResponse(savedRecord);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BorrowResponse> getMyCurrentBorrows() {
		String userId=getCurrentReader().getUserId();

		return borrowRecordRepository.findByUser_UserIdAndBorrowStatusInOrderByBorrowDateDesc(
				userId, Arrays.asList(BorrowStatus.BORROWED,BorrowStatus.RETURN_PENDING,BorrowStatus.OVERDUE))
				.stream().map(this::toBorrowResponse).collect(Collectors.toList());
	}

	@Override
	public void requestReturn(Long borrowId) {
		String userId=getCurrentReader().getUserId();
		
		BorrowRecord record=borrowRecordRepository.findById(borrowId)
				.orElseThrow(()->new LibraryBusinessException(ResponseCode.BORROW_RECORD_NOT_FOUND));
		if(!record.getUser().getUserId().equals(userId)) {
			throw new LibraryBusinessException(ResponseCode.BORROW_RECORD_NOT_BELONG_TO_USER);
		}
		//以下兩種狀態才是正常歸還，避免重複歸還
        if (record.getBorrowStatus() != BorrowStatus.BORROWED
                && record.getBorrowStatus() != BorrowStatus.OVERDUE) {
            throw new LibraryBusinessException(ResponseCode.BORROW_STATUS_INVALID);
        }
        //borrow跟copy都設定為等待歸還確認中
        record.setBorrowStatus(BorrowStatus.RETURN_PENDING);
        record.setReturnRequestDate(LocalDate.now());

        borrowRecordRepository.save(record);

        BookCopy copy = record.getBookCopy();
        copy.setCopyStatus(BookCopyStatus.RETURN_PENDING);

        bookCopyRepository.save(copy);
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<BorrowResponse> getAdminBorrows(BorrowStatus borrowStatus) {
		LoginUserHolder.requireAdmin();
		List<BorrowRecord> records;//透過狀態查看書籍
		if(borrowStatus==null) {
			records=borrowRecordRepository.findAllByOrderByCreatedAtDesc();
		} else {
			records=borrowRecordRepository.findByBorrowStatusOrderByCreatedAtDesc(borrowStatus);
		}
		return records.stream().map(this::toBorrowResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly=true)
	public List<BorrowResponse> getReturnPendingBorrows() {
		LoginUserHolder.requireAdmin();
		return borrowRecordRepository
				.findByBorrowStatusOrderByCreatedAtDesc(BorrowStatus.RETURN_PENDING)
				.stream().map(this::toBorrowResponse).collect(Collectors.toList());
	}

	@Override
	public BorrowResponse approveReturn(Long borrowId, BorrowStatus resultStatus) {
		LoginUserHolder.requireAdmin();

        BorrowRecord record = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BORROW_RECORD_NOT_FOUND));

        if (record.getBorrowStatus() != BorrowStatus.RETURN_PENDING) {
            throw new LibraryBusinessException(ResponseCode.BORROW_STATUS_INVALID);
        }

        if (resultStatus != BorrowStatus.RETURNED
                && resultStatus != BorrowStatus.DAMAGED
                && resultStatus != BorrowStatus.LOST) {
        	throw new LibraryBusinessException(
                    ResponseCode.BAD_REQUEST,"歸還審核結果只能是 RETURNED、DAMAGED 或 LOST");
        }

        BookCopy copy = record.getBookCopy();

        record.setBorrowStatus(resultStatus);
        record.setActualReturnDate(LocalDate.now());

        if (resultStatus == BorrowStatus.RETURNED) {
            copy.setCopyStatus(BookCopyStatus.AVAILABLE);
        } else if (resultStatus == BorrowStatus.DAMAGED) {
            copy.setCopyStatus(BookCopyStatus.DAMAGED);
        } else if (resultStatus == BorrowStatus.LOST) {
            copy.setCopyStatus(BookCopyStatus.LOST);
        }

        bookCopyRepository.save(copy);
        BorrowRecord savedRecord = borrowRecordRepository.save(record);

        notificationService.notifyReturnResult(savedRecord.getUser(), savedRecord);

        if (resultStatus == BorrowStatus.RETURNED) {
            notifyAdminsIfBookHasWaitingReservation(copy.getBook());
        }

        return toBorrowResponse(savedRecord);
	}
	
	@Override
	@Transactional
	public List<BorrowResponse> batchApproveNormalReturn(List<Long> borrowIds) {

	    LoginUserHolder.requireAdmin();

	    if (borrowIds == null || borrowIds.isEmpty()) {
	        throw new LibraryBusinessException(ResponseCode.BORROW_BATCH_EMPTY);
	    }

	    List<BorrowResponse> responses = new ArrayList<>();

	    for (Long borrowId : borrowIds) {

	        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowId)
	                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BORROW_RECORD_NOT_FOUND));

	        if (borrowRecord.getBorrowStatus() != BorrowStatus.RETURN_PENDING) {
	            throw new LibraryBusinessException(ResponseCode.BORROW_STATUS_INVALID);
	        }

	        BookCopy bookCopy = borrowRecord.getBookCopy();

	        borrowRecord.setBorrowStatus(BorrowStatus.RETURNED);
	        borrowRecord.setActualReturnDate(LocalDate.now());

	        bookCopy.setCopyStatus(BookCopyStatus.AVAILABLE);

	        BorrowRecord saved = borrowRecordRepository.save(borrowRecord);
	        bookCopyRepository.save(bookCopy);

	        notificationService.notifyReturnResult(saved.getUser(), saved);

	        // 如果這本書有等待預約，通知管理員可以處理下一順位
	        notifyAdminsIfBookHasWaitingReservation(bookCopy.getBook());

	        responses.add(toBorrowResponse(saved));
	    }

	    return responses;
	}
	
	private void notifyAdminsIfBookHasWaitingReservation(Book book) {

	    if (book == null || book.getBookId() == null) {
	        return;
	    }

	    String bookId = book.getBookId();

	    Reservation nextWaitingReservation = reservationRepository
	            .findFirstByBook_BookIdAndReservationStatusOrderByReservationDateAsc(
	                    bookId,
	                    ReservationStatus.WAITING
	            )
	            .orElse(null);

	    if (nextWaitingReservation == null) {
	        return;
	    }

	    long availableCount = bookCopyRepository.countByBook_BookIdAndCopyStatus(
	            bookId,
	            BookCopyStatus.AVAILABLE
	    );

	    if (availableCount <= 0) {
	        return;
	    }

	    notificationService.notifyAdminsReservationReady(
	            nextWaitingReservation,
	            availableCount
	    );
	}
	
	private void completeAvailableNoticeReservationIfExists(String userId, String bookId) {
	    reservationRepository
	            .findFirstByUser_UserIdAndBook_BookIdAndReservationStatusOrderByReservationDateAsc(
	                    userId,
	                    bookId,
	                    ReservationStatus.AVAILABLE_NOTICE
	            )
	            .ifPresent(reservation -> {
	                reservation.setReservationStatus(ReservationStatus.COMPLETED);
	                reservationRepository.save(reservation);
	            });
	}
	
	private User getCurrentReader() {
	    LoginUserHolder.requireReader();
	    String userId = LoginUserHolder.requireLoginUser().getUserId();
	    return userRepository.findById(userId)
	            .orElseThrow(() -> new LibraryBusinessException(ResponseCode.USER_NOT_FOUND));
	}
	
	private BorrowResponse toBorrowResponse(BorrowRecord record) {
	    BookCopy copy = record.getBookCopy();
	    Book book = copy.getBook();
	    User user = record.getUser();

	    BorrowResponse response = new BorrowResponse();

	    response.setBorrowId(record.getBorrowId());

	    response.setUserId(user.getUserId());
	    response.setUsername(user.getUsername());
	    response.setName(user.getName());

	    response.setBookId(book.getBookId());
	    response.setTitle(book.getTitle());

	    response.setCopyId(copy.getCopyId());
	    response.setCopyCode(copy.getCopyCode());

	    response.setBorrowDate(record.getBorrowDate());
	    response.setDueDate(record.getDueDate());
	    response.setReturnRequestDate(record.getReturnRequestDate());
	    response.setActualReturnDate(record.getActualReturnDate());

	    response.setBorrowStatus(record.getBorrowStatus().name());

	    return response;
	}
}
