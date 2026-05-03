package com.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.book.BookDetailResponse;
import com.library.dto.book.BookListResponse;
import com.library.dto.common.ApiResponse;
import com.library.service.BookService;

@RestController
@RequestMapping("/api/books")
public class BookController {
	
	@Autowired
	private BookService bookService;
	
	@GetMapping
	public ApiResponse<List<BookListResponse>> searchBooks(
			@RequestParam(required=false) String keyword,
			@RequestParam(required=false) String categoryId){
		return ApiResponse.success("查詢書籍成功", bookService.searchBooks(keyword, categoryId));
	}
	
	@GetMapping("/{bookId}")
	public ApiResponse<BookDetailResponse> getBookDetail(@PathVariable String bookId){
		return ApiResponse.success("查詢書籍詳情成功", bookService.getBookDetail(bookId));
	}
}
