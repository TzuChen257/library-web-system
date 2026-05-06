package com.library.controller.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.library.dto.book.BookListResponse;
import com.library.dto.book.BookRequest;
import com.library.dto.common.ApiResponse;
import com.library.entity.enums.StatusType;
import com.library.service.AdminBookImportService;
import com.library.service.AdminBookService;
import com.library.util.security.LoginUserHolder;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/admin/books")
@AllArgsConstructor
public class AdminBookController {
	
    private AdminBookService adminBookService;
    private AdminBookImportService adminBookImportService;

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
    
    /**
     * 下載書目與館藏匯入範本
     * GET /iread-library/api/admin/books/import-template
     */
    @GetMapping("/import-template")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        LoginUserHolder.requireAdmin();

        byte[] excelBytes = adminBookImportService.generateBookImportTemplate();

        String fileName = "book-import-template.xlsx";

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
    //批次上傳書目
    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importBooks(
            @RequestParam("file") MultipartFile file) {

        LoginUserHolder.requireAdmin();

        Map<String, Object> data = adminBookImportService.importBooks(file);

        return ApiResponse.success("書目與館藏匯入完成", data);
    }
}