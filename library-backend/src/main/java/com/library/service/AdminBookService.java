package com.library.service;

import java.util.List;

import com.library.dto.book.BookListResponse;
import com.library.dto.book.BookRequest;
import com.library.entity.enums.StatusType;

public interface AdminBookService {

    List<BookListResponse> getAdminBooks(String keyword, StatusType status);

    BookListResponse createBook(BookRequest request);

    BookListResponse updateBook(String bookId, BookRequest request);

    BookListResponse updateBookStatus(String bookId, StatusType status);
}