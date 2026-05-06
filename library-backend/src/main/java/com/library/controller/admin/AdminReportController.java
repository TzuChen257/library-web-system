package com.library.controller.admin;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.service.AdminReportService;
import com.library.util.security.LoginUserHolder;

@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {
	
	@Autowired
    private AdminReportService adminReportService;

    /**
     * 管理員下載年度借閱統計 Excel
     *
     * GET /iread-library/api/admin/reports/borrow-statistics.xlsx?year=2026&topN=10
     */
    @GetMapping("/borrow-statistics.xlsx")
    public ResponseEntity<byte[]> downloadBorrowStatisticsReport(
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "10") Integer topN) {

        LoginUserHolder.requireAdmin();

        int targetYear = year == null ? LocalDate.now().getYear() : year;
        int safeTopN = topN == null ? 10 : topN;

        byte[] excelBytes = adminReportService.generateBorrowStatisticsReport(
                targetYear,
                safeTopN
        );

        String fileName = "borrow-statistics-" + targetYear + ".xlsx";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\""
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(excelBytes);
    }
}