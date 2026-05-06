package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;

public interface NotificationService {
	void notifyBorrowSuccess(User receiver, BorrowRecord borrowRecord);

    void notifyReturnResult(User receiver, BorrowRecord borrowRecord);

    void notifyReservationAvailable(User receiver, Reservation reservation);

    void notifyDueSoon(User receiver, BorrowRecord borrowRecord);

    void notifyOverdue(User receiver, BorrowRecord borrowRecord);

    void notifyOverdueSevenDays(User receiver, BorrowRecord borrowRecord);
    
    void notifyReservationExpired(User receiver, Reservation reservation);

    void notifyAdminsReservationReady(Reservation reservation, long availableCount);
}
