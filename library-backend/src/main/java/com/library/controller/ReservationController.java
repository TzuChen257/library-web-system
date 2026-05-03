package com.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.dto.reservation.ReservationResponse;
import com.library.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
	
	@Autowired
	private ReservationService reservationService;
	
	@PostMapping
    public ApiResponse<ReservationResponse> reserveBook(@RequestParam String bookId) {
        return ApiResponse.success("預約成功",reservationService.reserveBook(bookId));
    }

    @GetMapping("/me")
    public ApiResponse<List<ReservationResponse>> getMyReservations() {
        return ApiResponse.success("查詢我的預約成功",reservationService.getMyReservations());
    }

    @PatchMapping("/{reservationId}/cancel")
    public ApiResponse<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ApiResponse.success("取消預約成功");
    }
}
