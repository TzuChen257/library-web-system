package com.library.service;

import java.util.List;

import com.library.dto.reservation.ReservationResponse;
import com.library.entity.enums.ReservationStatus;

public interface ReservationService {
	
	ReservationResponse reserveBook(String bookId);

    List<ReservationResponse> getMyReservations();

    void cancelReservation(Long reservationId);

    List<ReservationResponse> getAdminReservations(ReservationStatus reservationStatus);

    ReservationResponse notifyReservationAvailable(Long reservationId);
    
    void expireAvailableNoticeReservations();
}
