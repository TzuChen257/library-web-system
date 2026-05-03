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
	public void createBorrowSuccessMessage(User receiver, BorrowRecord borrowRecord) {
		Book book = borrowRecord.getBookCopy().getBook();

        Message message = new Message();

        message.setReceiver(receiver);
        message.setTitle("借閱成功通知");
        message.setContent("您已成功借閱《"+book.getTitle()
                        +"》，應歸還日為 "+borrowRecord.getDueDate()+"。");
        message.setMessageType(MessageType.BORROW);
        message.setIsRead(false);
        message.setRelatedBorrow(borrowRecord);

        messageRepository.save(message);
	}

	@Override
	public void createReturnResultMessage(User receiver, BorrowRecord borrowRecord) {
		Book book = borrowRecord.getBookCopy().getBook();

        Message message = new Message();

        message.setReceiver(receiver);
        message.setTitle("歸還審核通知");
        message.setContent("您借閱的《"+book.getTitle()+"》歸還狀態已更新為："
                        +borrowRecord.getBorrowStatus().name()+"。");
        message.setMessageType(MessageType.RETURN);
        message.setIsRead(false);
        message.setRelatedBorrow(borrowRecord);

        messageRepository.save(message);
	}
	
	@Override
	public void createReservationNoticeMessage(User receiver, Reservation reservation) {
		Book book = reservation.getBook();
	    Message message = new Message();

	    message.setReceiver(receiver);
	    message.setTitle("預約書籍可借通知");
	    message.setContent("您預約的《" + book.getTitle()
	            + "》目前已有可借館藏，請於 "
	            + reservation.getExpireDate()
	            + " 前完成借閱或洽管理員處理。");
	    message.setMessageType(MessageType.RESERVATION);
	    message.setIsRead(false);
	    message.setRelatedReservation(reservation);

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
