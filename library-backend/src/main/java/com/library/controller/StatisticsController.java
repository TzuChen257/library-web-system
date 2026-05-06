package com.library.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.service.StatisticsService;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {
	
	@Autowired
    private StatisticsService statisticsService;

    /**
     * 首頁公開統計
     * GET /iread-library/api/statistics/public/summary
     */
    @GetMapping("/public/summary")
    public ApiResponse<Map<String, Object>> getPublicSummary() {
        Map<String, Object> data = statisticsService.getPublicSummary();
        return ApiResponse.success("查詢首頁公開統計成功", data);
    }
    /**
     * 首頁本月熱門借閱 Top N
     * GET /iread-library/api/statistics/public/top-borrowed-books?limit=5
     */
    @GetMapping("/public/top-borrowed-books")
    public ApiResponse<List<Map<String, Object>>> getPublicTopBorrowedBooks(
            @RequestParam(defaultValue = "5") Integer limit) {
        List<Map<String, Object>> data = statisticsService.getPublicTopBorrowedBooks(limit);
        return ApiResponse.success("查詢本月熱門借閱書籍成功", data);
    }
}