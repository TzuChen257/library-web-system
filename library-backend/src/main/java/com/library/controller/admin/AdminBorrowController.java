package com.library.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.borrow.BorrowResponse;
import com.library.dto.common.ApiResponse;
import com.library.entity.enums.BorrowStatus;
import com.library.service.BorrowService;

@RestController
@RequestMapping("/api/admin/borrows")
public class AdminBorrowController {
	@Autowired
	private BorrowService borrowService;
	
	@GetMapping
    public ApiResponse<List<BorrowResponse>> getAdminBorrows(
            @RequestParam(required = false) BorrowStatus borrowStatus) {
        return ApiResponse.success(
                "查詢借閱紀錄成功",borrowService.getAdminBorrows(borrowStatus));
    }

    @GetMapping("/return-pending")
    public ApiResponse<List<BorrowResponse>> getReturnPendingBorrows() {
        return ApiResponse.success(
                "查詢歸還待審核清單成功",borrowService.getReturnPendingBorrows());
    }

    @PatchMapping("/{borrowId}/approve-return")
    public ApiResponse<BorrowResponse> approveReturn(
            @PathVariable Long borrowId,
            @RequestParam BorrowStatus resultStatus) {
        return ApiResponse.success(
                "歸還審核完成",borrowService.approveReturn(borrowId, resultStatus));
    }
    
    @PatchMapping("/approve-return/batch-normal")
    public ApiResponse<List<BorrowResponse>> batchApproveNormalReturn(
            @RequestBody List<Long> borrowIds) {

        List<BorrowResponse> data = borrowService.batchApproveNormalReturn(borrowIds);

        return ApiResponse.success("批次正常歸還審核完成", data);
    }
}
