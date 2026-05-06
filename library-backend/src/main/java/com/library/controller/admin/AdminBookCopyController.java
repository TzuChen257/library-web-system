package com.library.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.book.BookCopyResponse;
import com.library.dto.common.ApiResponse;
import com.library.entity.enums.BookCopyStatus;
import com.library.service.AdminBookCopyService;

@RestController
@RequestMapping("/api/admin")
public class AdminBookCopyController {
	
	@Autowired
    private AdminBookCopyService adminBookCopyService;

    @GetMapping("/book-copies")
    public ApiResponse<List<BookCopyResponse>> getAdminBookCopies(
            @RequestParam(required = false) String bookId,
            @RequestParam(required = false) BookCopyStatus copyStatus,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(
                "查詢館藏冊本成功",
                adminBookCopyService.getAdminBookCopies(bookId, copyStatus, keyword)
        );
    }

    @GetMapping("/book-copies/{copyId}")
    public ApiResponse<BookCopyResponse> getBookCopyById(
            @PathVariable String copyId
    ) {
        return ApiResponse.success(
                "查詢館藏冊本成功",
                adminBookCopyService.getBookCopyById(copyId)
        );
    }

    @PostMapping("/books/{bookId}/copies")
    public ApiResponse<BookCopyResponse> createBookCopy(
            @PathVariable String bookId,
            @RequestParam String copyCode,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String note
    ) {
        return ApiResponse.success(
                "新增館藏冊本成功",
                adminBookCopyService.createBookCopy(bookId, copyCode, location, note)
        );
    }

    @PutMapping("/book-copies/{copyId}")
    public ApiResponse<BookCopyResponse> updateBookCopy(
            @PathVariable String copyId,
            @RequestParam(required = false) String copyCode,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BookCopyStatus copyStatus,
            @RequestParam(required = false) String note
    ) {
        return ApiResponse.success(
                "修改館藏冊本成功",
                adminBookCopyService.updateBookCopy(
                        copyId,
                        copyCode,
                        location,
                        copyStatus,
                        note
                )
        );
    }

    @PatchMapping("/book-copies/{copyId}/status")
    public ApiResponse<BookCopyResponse> updateBookCopyStatus(
            @PathVariable String copyId,
            @RequestParam BookCopyStatus copyStatus
    ) {
        return ApiResponse.success(
                "修改館藏狀態成功",
                adminBookCopyService.updateBookCopyStatus(copyId, copyStatus)
        );
    }
}