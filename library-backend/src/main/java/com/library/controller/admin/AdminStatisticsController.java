package com.library.controller.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.service.AdminStatisticsService;
import com.library.util.security.LoginUserHolder;


@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

	@Autowired
    private AdminStatisticsService adminStatisticsService;

    /**
     * 管理員後台統計摘要
     * GET /iread-library/api/admin/statistics/summary?year=2026&month=5
     */
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> getSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        LoginUserHolder.requireAdmin();

        Map<String, Object> data = adminStatisticsService.getSummary(year, month);

        return ApiResponse.success("查詢管理員統計摘要成功", data);
    }
}