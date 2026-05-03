package com.library.service;

import java.util.List;

import com.library.dto.message.MessageResponse;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;

public interface MessageService {
	void createBorrowSuccessMessage(User receiver, BorrowRecord borrowRecord);
	void createReturnResultMessage(User receiver, BorrowRecord borrowRecord);
	void createReservationNoticeMessage(User receiver, Reservation reservation);
	List<MessageResponse> getMyMessages();
	long getMyUnreadCount();
	void markAsRead(Long messageId);
}