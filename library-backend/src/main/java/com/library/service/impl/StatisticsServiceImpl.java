package com.library.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.StatusType;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.service.StatisticsService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private BookRepository bookRepository;
    private BookCopyRepository bookCopyRepository;
    private BorrowRecordRepository borrowRecordRepository;

    @Override
    public Map<String, Object> getPublicSummary() {
        LocalDate today = LocalDate.now();

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long totalBookCount = bookRepository.countByStatus(StatusType.ACTIVE);

        long availableCopyCount = bookCopyRepository.countByCopyStatus(
                BookCopyStatus.AVAILABLE
        );

        long todayBorrowCount = borrowRecordRepository.countByBorrowDate(today);

        long monthBorrowCount = borrowRecordRepository.countByBorrowDateBetween(
                monthStart,
                monthEnd
        );

        Map<String, Object> result = new HashMap<>();
        result.put("totalBookCount", totalBookCount);
        result.put("availableCopyCount", availableCopyCount);
        result.put("todayBorrowCount", todayBorrowCount);
        result.put("monthBorrowCount", monthBorrowCount);

        return result;
    }
    
    @Override
    public List<Map<String, Object>> getPublicTopBorrowedBooks(Integer limit) {
        LocalDate today = LocalDate.now();

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        int safeLimit = limit == null || limit <= 0 ? 5 : limit;
        safeLimit = Math.min(safeLimit, 20);

        Pageable pageable = PageRequest.of(0, safeLimit);

        List<Object[]> rows = borrowRecordRepository.findTopBorrowedBooks(
                monthStart,
                monthEnd,
                pageable
        );

        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> item = new HashMap<>();

            item.put("bookId", row[0]);
            item.put("title", row[1]);
            item.put("author", row[2]);
            item.put("borrowCount", row[3]);

            result.add(item);
        }

        return result;
    }
}