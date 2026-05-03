package com.library.service;

import java.util.List;

import com.library.dto.book.BookDetailResponse;
import com.library.dto.book.BookListResponse;

public interface BookService {
	//每本書計算總館藏數與可借數
	List<BookListResponse> searchBooks(String keyword,String categoryId);
	//查單本書、查全部 copies、計算統計
	BookDetailResponse getBookDetail(String bookId);
}
