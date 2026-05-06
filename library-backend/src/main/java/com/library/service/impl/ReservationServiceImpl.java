package com.library.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.reservation.ReservationResponse;
import com.library.entity.Book;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import com.library.service.NotificationService;
import com.library.service.ReservationService;
import com.library.util.security.LoginUserHolder;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class ReservationServiceImpl implements ReservationService{
	
	private static final int RESERVATION_EXPIRE_HOURS = 48;
	
	private ReservationRepository reservationRepository;
    private BookRepository bookRepository;
    private BookCopyRepository bookCopyRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;

	@Override
	public ReservationResponse reserveBook(String bookId) {
		User currentReader=getCurrentReader();
		Book book=bookRepository.findById(bookId)
				.orElseThrow(()->new LibraryBusinessException(ResponseCode.BOOK_NOT_FOUND));
		long duplicatedCount = reservationRepository
                .countByUser_UserIdAndBook_BookIdAndReservationStatusIn(
                        currentReader.getUserId(),bookId,
                        Arrays.asList(ReservationStatus.WAITING,ReservationStatus.AVAILABLE_NOTICE));

        if (duplicatedCount > 0) {
            throw new LibraryBusinessException(ResponseCode.RESERVATION_DUPLICATED);
        }

        long queueCount = reservationRepository.countByBook_BookIdAndReservationStatusIn(
                bookId,Arrays.asList(ReservationStatus.WAITING,ReservationStatus.AVAILABLE_NOTICE));

        Reservation reservation = new Reservation();
        reservation.setUser(currentReader);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setReservationStatus(ReservationStatus.WAITING);
        reservation.setQueueOrder((int) queueCount + 1);

        Reservation savedReservation = reservationRepository.save(reservation);

        return toReservationResponse(savedReservation);
	}

	@Override
	public List<ReservationResponse> getMyReservations() {
		User currentReader = getCurrentReader();
        return reservationRepository.findByUser_UserIdOrderByCreatedAtDesc(currentReader.getUserId())
                .stream().map(this::toReservationResponse).collect(Collectors.toList());
	}

	@Override
	public void cancelReservation(Long reservationId) {
		User currentReader = getCurrentReader();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.RESERVATION_NOT_FOUND));

        if (!reservation.getUser().getUserId().equals(currentReader.getUserId())) {
            throw new LibraryBusinessException(ResponseCode.FORBIDDEN);
        }

        if (reservation.getReservationStatus() != ReservationStatus.WAITING
                && reservation.getReservationStatus() != ReservationStatus.AVAILABLE_NOTICE) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST,"此預約狀態不可取消");
        }

        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReservationResponse> getAdminReservations(ReservationStatus reservationStatus) {

	    List<Reservation> reservations;

	    if (reservationStatus == null) {
	        reservations = reservationRepository.findAllByOrderByCreatedAtDesc();
	    } else {
	        reservations = reservationRepository.findByReservationStatusOrderByCreatedAtDesc(reservationStatus);
	    }

	    return reservations.stream()
	            .map(this::toAdminReservationResponse)
	            .collect(Collectors.toList());
	}

	@Override
	@Transactional
	public ReservationResponse notifyReservationAvailable(Long reservationId) {

	    // 1. 管理員權限確認
	    LoginUserHolder.requireAdmin();

	    // 2. 查預約
	    Reservation reservation = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> new LibraryBusinessException(ResponseCode.RESERVATION_NOT_FOUND));

	    // 3. 只能通知 WAITING 狀態
	    if (reservation.getReservationStatus() != ReservationStatus.WAITING) {
	        throw new LibraryBusinessException(ResponseCode.RESERVATION_STATUS_INVALID);
	    }

	    String bookId = reservation.getBook().getBookId();

	    // 4. 必須是該書目前 WAITING 第一順位
	    Reservation firstWaiting = reservationRepository
	            .findFirstByBook_BookIdAndReservationStatusOrderByReservationDateAsc(
	                    bookId,
	                    ReservationStatus.WAITING
	            )
	            .orElseThrow(() -> new LibraryBusinessException(ResponseCode.RESERVATION_NOT_FOUND));

	    if (!firstWaiting.getReservationId().equals(reservation.getReservationId())) {
	        throw new LibraryBusinessException(ResponseCode.RESERVATION_NOT_FIRST_QUEUE);
	    }

	    // 5. 必須真的有 AVAILABLE 館藏
	    long availableCount = bookCopyRepository.countByBook_BookIdAndCopyStatus(
	            bookId,
	            BookCopyStatus.AVAILABLE
	    );

	    if (availableCount <= 0) {
	        throw new LibraryBusinessException(ResponseCode.NO_AVAILABLE_COPY_FOR_RESERVATION);
	    }

	    // 6. 改為已通知可取，並設定 48 小時後過期
	    LocalDateTime now = LocalDateTime.now();

	    reservation.setReservationStatus(ReservationStatus.AVAILABLE_NOTICE);
	    reservation.setExpireDate(now.plusHours(RESERVATION_EXPIRE_HOURS));

	    Reservation saved = reservationRepository.save(reservation);

	    // 7. 通知該讀者
	    notificationService.notifyReservationAvailable(saved.getUser(), saved);

	    return toReservationResponse(saved);
	}
	
	@Override
	@Transactional
	public void expireAvailableNoticeReservations() {

	    LocalDateTime now = LocalDateTime.now();

	    List<Reservation> expiredReservations =
	            reservationRepository.findByReservationStatusAndExpireDateBefore(
	                    ReservationStatus.AVAILABLE_NOTICE,
	                    now
	            );

	    for (Reservation reservation : expiredReservations) {
	        reservation.setReservationStatus(ReservationStatus.EXPIRED);

	        Reservation saved = reservationRepository.save(reservation);

	        // 通知原預約讀者：預約取書期限已過
	        notificationService.notifyReservationExpired(saved.getUser(), saved);

	        // 通知管理員：可處理下一順位
	        notifyAdminsIfNextReservationCanBeNotified(saved.getBook());
	    }
	}
	
	private void notifyAdminsIfNextReservationCanBeNotified(Book book) {

	    Reservation nextWaiting = reservationRepository
	            .findFirstByBook_BookIdAndReservationStatusOrderByReservationDateAsc(
	                    book.getBookId(),
	                    ReservationStatus.WAITING
	            )
	            .orElse(null);

	    if (nextWaiting == null) {
	        return;
	    }

	    long availableCount = bookCopyRepository.countByBook_BookIdAndCopyStatus(
	            book.getBookId(),
	            BookCopyStatus.AVAILABLE
	    );

	    if (availableCount <= 0) {
	        return;
	    }

	    notificationService.notifyAdminsReservationReady(nextWaiting, availableCount);
	}
	
	private User getCurrentReader() {
        LoginUserHolder.requireReader();
        String userId = LoginUserHolder.requireLoginUser().getUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.USER_NOT_FOUND));
    }

    private ReservationResponse toReservationResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();

        User user = reservation.getUser();
        Book book = reservation.getBook();

        response.setReservationId(reservation.getReservationId());

        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());

        response.setBookId(book.getBookId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());

        response.setReservationDate(reservation.getReservationDate());
        response.setExpireDate(reservation.getExpireDate());
        response.setReservationStatus(reservation.getReservationStatus().name());
        response.setQueueOrder(reservation.getQueueOrder());

        return response;
    }
    
    private ReservationResponse toAdminResponse(
            Reservation reservation,
            int availableCopyCount,
            boolean firstInQueue,
            boolean canNotify
    ) {
        ReservationResponse response = toReservationResponse(reservation);

        response.setAvailableCopyCount(availableCopyCount);
        response.setFirstInQueue(firstInQueue);
        response.setCanNotify(canNotify);

        return response;
    }
    
    private ReservationResponse toAdminReservationResponse(Reservation reservation) {

        String bookId = reservation.getBook().getBookId();

        long availableCount = bookCopyRepository.countByBook_BookIdAndCopyStatus(
                bookId,
                BookCopyStatus.AVAILABLE
        );

        boolean firstInQueue = isFirstWaitingReservation(reservation);

        boolean canNotify =
                reservation.getReservationStatus() == ReservationStatus.WAITING
                && firstInQueue
                && availableCount > 0;

        return toAdminResponse(
                reservation,
                (int) availableCount,
                firstInQueue,
                canNotify
        );
    }
    
    private boolean isFirstWaitingReservation(Reservation reservation) {

        if (reservation.getReservationStatus() != ReservationStatus.WAITING) {
            return false;
        }

        return reservationRepository
                .findFirstByBook_BookIdAndReservationStatusOrderByReservationDateAsc(
                        reservation.getBook().getBookId(),
                        ReservationStatus.WAITING
                )
                .map(first -> first.getReservationId().equals(reservation.getReservationId()))
                .orElse(false);
    }

}
