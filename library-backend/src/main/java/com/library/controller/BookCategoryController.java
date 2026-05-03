package com.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.entity.BookCategory;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BookCategoryRepository;

@RestController
@RequestMapping("/api/book-categories")
public class BookCategoryController {
	
	@Autowired
	private BookCategoryRepository categoryService;
	
	@GetMapping
    public ApiResponse<List<BookCategory>> getAllCategories() {
        return ApiResponse.success("查詢書籍分類成功",categoryService.findAll());
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<BookCategory> getCategoryById(@PathVariable String categoryId) {
        return ApiResponse.success("查詢書籍分類成功",categoryService.findById(categoryId)
        		.orElseThrow(()->new LibraryBusinessException(ResponseCode.CATEGORY_NOT_FOUND)));
    }
	
}
