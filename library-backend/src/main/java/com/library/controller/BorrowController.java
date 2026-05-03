package com.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.borrow.BorrowResponse;
import com.library.dto.common.ApiResponse;
import com.library.service.impl.BorrowServiceImpl;

@RestController
@RequestMapping("/api/borrows")
public class BorrowController {
	
	@Autowired
	private BorrowServiceImpl borrowService;
	
	@PostMapping("/{bookId}")
	public ApiResponse<BorrowResponse> borrowBook(@PathVariable String bookId){
		return ApiResponse.success("借閱成功", borrowService.borrowBook(bookId));
	}
	
	@GetMapping("/me/current")
	public ApiResponse<List<BorrowResponse>> getMyCurrentBorrows(){
		return ApiResponse.success("查詢目前借閱成功", borrowService.getMyCurrentBorrows());
	}
	
	@PatchMapping("/{borrowId}/return-request")
	public ApiResponse<Void> requestReturn(@PathVariable Long borrowId){
		borrowService.requestReturn(borrowId);
		return ApiResponse.success("已送出歸還申請");
	}
}
