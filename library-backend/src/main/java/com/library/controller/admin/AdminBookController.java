package com.library.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.book.BookListResponse;
import com.library.dto.book.BookRequest;
import com.library.dto.common.ApiResponse;
import com.library.entity.enums.StatusType;
import com.library.service.AdminBookService;

@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {
	
	@Autowired
    private AdminBookService adminBookService;

    @GetMapping
    public ApiResponse<List<BookListResponse>> getAdminBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) StatusType status
    ) {
        return ApiResponse.success(
                "查詢書目成功",
                adminBookService.getAdminBooks(keyword, status)
        );
    }

    @PostMapping
    public ApiResponse<BookListResponse> createBook(
            @RequestBody BookRequest request
    ) {
        return ApiResponse.success(
                "新增書目成功",
                adminBookService.createBook(request)
        );
    }

    @PutMapping("/{bookId}")
    public ApiResponse<BookListResponse> updateBook(
            @PathVariable String bookId,
            @RequestBody BookRequest request
    ) {
        return ApiResponse.success(
                "修改書目成功",
                adminBookService.updateBook(bookId, request)
        );
    }

    @PatchMapping("/{bookId}/status")
    public ApiResponse<BookListResponse> updateBookStatus(
            @PathVariable String bookId,
            @RequestParam StatusType status
    ) {
        return ApiResponse.success(
                "修改書目狀態成功",
                adminBookService.updateBookStatus(bookId, status)
        );
    }
}