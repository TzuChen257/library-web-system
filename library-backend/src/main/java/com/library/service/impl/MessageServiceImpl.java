package com.library.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.message.MessageResponse;
import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Message;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.entity.enums.MessageType;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.MessageRepository;
import com.library.service.MessageService;
import com.library.util.security.LoginUserHolder;

@Service
@Transactional
public class MessageServiceImpl implements MessageService{
	
	@Autowired
	private MessageRepository messageRepository;

	@Override
	public void createSystemMessage(
	        User receiver,
	        String title,
	        String content,
	        MessageType messageType,
	        BorrowRecord relatedBorrow,
	        Reservation relatedReservation
	) {
	    Message message = new Message();

	    message.setReceiver(receiver);
	    message.setTitle(title);
	    message.setContent(content);
	    message.setMessageType(messageType);
	    message.setIsRead(false);
	    message.setRelatedBorrow(relatedBorrow);
	    message.setRelatedReservation(relatedReservation);

	    messageRepository.save(message);
	}

	@Override
	@Transactional(readOnly = true)
	public List<MessageResponse> getMyMessages() {
		String userId=LoginUserHolder.requireLoginUser().getUserId();
		return messageRepository.findByReceiver_UserIdOrderByCreatedAtDesc(userId)
				.stream().map(this::toMessageResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public long getMyUnreadCount() {
		String userId=LoginUserHolder.requireLoginUser().getUserId();
		return messageRepository.countByReceiver_UserIdAndIsReadFalse(userId);
	}

	@Override
	public void markAsRead(Long messageId) {
		String userId=LoginUserHolder.requireLoginUser().getUserId();
		
		Message message=messageRepository.findByMessageIdAndReceiver_UserId(messageId, userId)
				.orElseThrow(()->new LibraryBusinessException(ResponseCode.MESSAGE_NOT_FOUND));
		
		if(!message.getIsRead().booleanValue()) {
			message.setIsRead(true);
			message.setReadAt(LocalDateTime.now());
			messageRepository.save(message);
		}
	}
	
	@Override
	public void deleteMyMessage(Long messageId) {
		String userId=LoginUserHolder.requireLoginUser().getUserId();

	    Message message = messageRepository
	            .findByMessageIdAndReceiver_UserId(messageId, userId)
	            .orElseThrow(() -> new LibraryBusinessException(ResponseCode.MESSAGE_NOT_FOUND));

	    messageRepository.delete(message);
	}
	
	private MessageResponse toMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();

        response.setMessageId(message.getMessageId());
        response.setTitle(message.getTitle());
        response.setContent(message.getContent());
        response.setMessageType(message.getMessageType().name());
        response.setIsRead(message.getIsRead());
        response.setCreatedAt(message.getCreatedAt());
        response.setReadAt(message.getReadAt());

        BorrowRecord relatedBorrow = message.getRelatedBorrow();
        if (relatedBorrow != null) {
            response.setRelatedBorrowId(relatedBorrow.getBorrowId());
        }

        Reservation relatedReservation = message.getRelatedReservation();
        if (relatedReservation != null) {
            response.setRelatedReservationId(relatedReservation.getReservationId());
        }

        return response;
    }

}
