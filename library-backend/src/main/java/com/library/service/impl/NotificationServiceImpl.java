package com.library.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.library.entity.Book;
import com.library.entity.BorrowRecord;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.entity.enums.MessageType;
import com.library.service.MailService;
import com.library.service.MessageService;
import com.library.service.NotificationService;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private MessageService messageService;
    private MailService mailService;

    @Override
    public void notifyBorrowSuccess(User receiver, BorrowRecord borrowRecord) {
        Book book = borrowRecord.getBookCopy().getBook();

        String title = "借閱成功通知";
        String content = "您已成功借閱《" + book.getTitle()
                + "》，應歸還日為 " + borrowRecord.getDueDate() + "。";

        messageService.createSystemMessage(
                receiver,
                title,
                content,
                MessageType.BORROW,
                borrowRecord,
                null
        );
    }

    @Override
    public void notifyReturnResult(User receiver, BorrowRecord borrowRecord) {
        Book book = borrowRecord.getBookCopy().getBook();

        String title = "歸還審核通知";
        String content = "您借閱的《" + book.getTitle()
                + "》歸還狀態已更新為："
                + borrowRecord.getBorrowStatus().name() + "。";

        messageService.createSystemMessage(
                receiver,
                title,
                content,
                MessageType.RETURN,
                borrowRecord,
                null
        );
    }

    @Override
    public void notifyReservationAvailable(User receiver, Reservation reservation) {
        Book book = reservation.getBook();

        String title = "預約書籍可借通知";
        String content = "您預約的《" + book.getTitle()
                + "》目前已有可借館藏，請於 "
                + reservation.getExpireDate()
                + " 前完成借閱或洽管理員處理。";

        messageService.createSystemMessage(
                receiver,
                title,
                content,
                MessageType.RESERVATION,
                null,
                reservation
        );

        sendMailSafely(receiver, title, content);
    }

    @Override
    public void notifyDueSoon(User receiver, BorrowRecord borrowRecord) {
        Book book = borrowRecord.getBookCopy().getBook();

        String title = "借閱書籍即將到期通知";
        String content = "親愛的 " + receiver.getName() + " 您好：\n\n"
                + "您借閱的《" + book.getTitle() + "》將於 "
                + borrowRecord.getDueDate()
                + " 到期，請記得準時歸還。\n\n"
                + "圖書館館藏管理系統";

        messageService.createSystemMessage(
                receiver,
                title,
                content,
                MessageType.DUE_SOON,
                borrowRecord,
                null
        );

        sendMailSafely(receiver, title, content);
    }

    @Override
    public void notifyOverdue(User receiver, BorrowRecord borrowRecord) {
        Book book = borrowRecord.getBookCopy().getBook();

        String title = "借閱書籍逾期通知";
        String content = "親愛的 " + receiver.getName() + " 您好：\n\n"
                + "您借閱的《" + book.getTitle() + "》已逾期，原到期日為 "
                + borrowRecord.getDueDate()
                + "。請盡快歸還書籍，以免影響後續借閱權限。\n\n"
                + "圖書館館藏管理系統";

        messageService.createSystemMessage(
                receiver,
                title,
                content,
                MessageType.OVERDUE,
                borrowRecord,
                null
        );

        sendMailSafely(receiver, title, content);
    }

    @Override
    public void notifyOverdueSevenDays(User receiver, BorrowRecord borrowRecord) {
        Book book = borrowRecord.getBookCopy().getBook();

        String title = "借閱逾期 7 日通知與借書功能暫停";
        String content = "親愛的 " + receiver.getName() + " 您好：\n\n"
                + "您借閱的《" + book.getTitle() + "》已逾期 7 日以上，原到期日為 "
                + borrowRecord.getDueDate()
                + "。\n\n"
                + "系統已暫停您的借書功能。請盡快聯繫管理員完成還書與借閱權限開通。\n\n"
                + "圖書館館藏管理系統";

        messageService.createSystemMessage(
                receiver,
                title,
                content,
                MessageType.OVERDUE,
                borrowRecord,
                null
        );

        sendMailSafely(receiver, title, content);
    }

    private void sendMailSafely(User receiver, String title, String content) {
        if (receiver == null || !StringUtils.hasText(receiver.getEmail())) {
            return;
        }

        try {
            mailService.sendMail(receiver.getEmail(), title, content);
        } catch (Exception e) {
            log.warn("寄送 Email 失敗，receiverId={}, email={}, title={}",
                    receiver.getUserId(),
                    receiver.getEmail(),
                    title,
                    e
            );
        }
    }
}