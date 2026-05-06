package com.library.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.entity.enums.BorrowStatus;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import com.library.service.NotificationService;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class OverdueNoticeScheduler {

    private BorrowRecordRepository borrowRecordRepository;
    private UserRepository userRepository;
    private NotificationService notificationService;


    /**
     * 每天早上 9 點檢查：
     * 1. 到期前一天通知
     * 2. 逾期當下通知
     * 3. 逾期 7 日通知並暫停借書
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void checkBorrowDueAndOverdue() {
        LocalDate today = LocalDate.now();

        sendDueSoonNotice(today.plusDays(1));

        sendOverdueNotice(today.minusDays(1));

        sendOverdueSevenDaysNotice(today.minusDays(7));
    }

    private void sendDueSoonNotice(LocalDate dueDate) {
        List<BorrowRecord> records =
                borrowRecordRepository.findByBorrowStatusAndDueDateAndDueSoonNoticeSentAtIsNull(
                        BorrowStatus.BORROWED,
                        dueDate
                );

        for (BorrowRecord record : records) {
            notificationService.notifyDueSoon(record.getUser(), record);

            record.setDueSoonNoticeSentAt(LocalDateTime.now());
            borrowRecordRepository.save(record);
        }
    }

    private void sendOverdueNotice(LocalDate overdueDate) {
        List<BorrowRecord> records =
                borrowRecordRepository.findByBorrowStatusAndDueDateAndOverdueNoticeSentAtIsNull(
                        BorrowStatus.BORROWED,
                        overdueDate
                );

        for (BorrowRecord record : records) {
            record.setBorrowStatus(BorrowStatus.OVERDUE);

            notificationService.notifyOverdue(record.getUser(), record);

            record.setOverdueNoticeSentAt(LocalDateTime.now());
            borrowRecordRepository.save(record);
        }
    }

    private void sendOverdueSevenDaysNotice(LocalDate dueDateLessThanOrEqual) {
        List<BorrowRecord> records =
                borrowRecordRepository.findByBorrowStatusAndDueDateLessThanEqualAndOverdue7NoticeSentAtIsNull(
                        BorrowStatus.OVERDUE,
                        dueDateLessThanOrEqual
                );

        for (BorrowRecord record : records) {
            User user = record.getUser();

            user.setBorrowSuspended(true);
            userRepository.save(user);

            notificationService.notifyOverdueSevenDays(user, record);

            record.setOverdue7NoticeSentAt(LocalDateTime.now());
            borrowRecordRepository.save(record);
        }
    }
}