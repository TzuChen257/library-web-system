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
import com.library.entity.enums.ReservationStatus;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
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
	public List<ReservationResponse> getAdminReservations(ReservationStatus reservationStatus) {
		LoginUserHolder.requireAdmin();

        List<Reservation> reservations;

        if (reservationStatus == null) {
            reservations = reservationRepository.findAllByOrderByCreatedAtDesc();
        } else {
            reservations = reservationRepository
                    .findByReservationStatusOrderByCreatedAtDesc(reservationStatus);
        }

        return reservations.stream().map(this::toReservationResponse).collect(Collectors.toList());
	}

	@Override
	public ReservationResponse notifyReservationAvailable(Long reservationId) {
		LoginUserHolder.requireAdmin();

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.RESERVATION_NOT_FOUND));

        if (reservation.getReservationStatus() != ReservationStatus.WAITING) {
            throw new LibraryBusinessException(
            		ResponseCode.BAD_REQUEST,"只有 WAITING 狀態的預約可以通知");
        }

        reservation.setReservationStatus(ReservationStatus.AVAILABLE_NOTICE);
        reservation.setExpireDate(LocalDateTime.now().plusHours(RESERVATION_EXPIRE_HOURS));

        Reservation savedReservation = reservationRepository.save(reservation);

        notificationService.notifyReservationAvailable(
                savedReservation.getUser(),
                savedReservation
        );

        return toReservationResponse(savedReservation);
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

}
