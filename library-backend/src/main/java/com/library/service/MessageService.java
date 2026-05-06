package com.library.service;

import java.util.List;

import com.library.dto.message.MessageResponse;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.entity.enums.MessageType;

public interface MessageService {
	void createSystemMessage(
            User receiver,
            String title,
            String content,
            MessageType messageType,
            BorrowRecord relatedBorrow,
            Reservation relatedReservation
    );

    List<MessageResponse> getMyMessages();

    long getMyUnreadCount();

    void markAsRead(Long messageId);

    void deleteMyMessage(Long messageId);
}