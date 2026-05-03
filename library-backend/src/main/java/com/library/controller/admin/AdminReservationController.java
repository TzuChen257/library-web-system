package com.library.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.dto.reservation.ReservationResponse;
import com.library.entity.enums.ReservationStatus;
import com.library.service.ReservationService;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {
	@Autowired
	private ReservationService reservationService;
	
	@GetMapping
    public ApiResponse<List<ReservationResponse>> getAdminReservations(
            @RequestParam(required = false) ReservationStatus reservationStatus) {
        return ApiResponse.success(
        		"查詢預約清單成功",reservationService.getAdminReservations(reservationStatus));
    }

    @PatchMapping("/{reservationId}/notify")
    public ApiResponse<ReservationResponse> notifyReservationAvailable(@PathVariable Long reservationId) {
        return ApiResponse.success(
        		"已通知讀者可借閱",reservationService.notifyReservationAvailable(reservationId));
    }

}
