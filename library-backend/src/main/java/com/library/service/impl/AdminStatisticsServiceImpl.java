package com.library.service.impl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.library.entity.enums.BorrowStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReservationRepository;
import com.library.service.AdminStatisticsService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AdminStatisticsServiceImpl implements AdminStatisticsService {

    private BorrowRecordRepository borrowRecordRepository;
    private ReservationRepository reservationRepository;

    @Override
    public Map<String, Object> getSummary(Integer year, Integer month) {
        LocalDate today = LocalDate.now();

        int targetYear = year == null ? today.getYear() : year;
        int targetMonth = month == null ? today.getMonthValue() : month;

        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        long borrowingCount = borrowRecordRepository.countByBorrowStatus(BorrowStatus.BORROWED);

        long overdueCount = borrowRecordRepository.countByBorrowStatus(BorrowStatus.OVERDUE);

        long returnPendingCount = borrowRecordRepository.countByBorrowStatus(BorrowStatus.RETURN_PENDING);

        long waitingReservationCount = reservationRepository.countByReservationStatus(
                ReservationStatus.WAITING
        );

        long availableNoticeCount = reservationRepository.countByReservationStatus(
                ReservationStatus.AVAILABLE_NOTICE
        );

        long todayBorrowCount = borrowRecordRepository.countByBorrowDate(today);

        long monthBorrowCount = borrowRecordRepository.countByBorrowDateBetween(
                monthStart,
                monthEnd
        );

        Map<String, Object> result = new HashMap<>();
        result.put("borrowingCount", borrowingCount);
        result.put("overdueCount", overdueCount);
        result.put("returnPendingCount", returnPendingCount);
        result.put("waitingReservationCount", waitingReservationCount);
        result.put("availableNoticeCount", availableNoticeCount);
        result.put("todayBorrowCount", todayBorrowCount);
        result.put("monthBorrowCount", monthBorrowCount);

        return result;
    }
}