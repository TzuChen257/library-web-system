package com.library.service;

import java.util.List;

import com.library.dto.book.BookCopyResponse;
import com.library.entity.enums.BookCopyStatus;

public interface AdminBookCopyService {

    List<BookCopyResponse> getAdminBookCopies(
            String bookId,
            BookCopyStatus copyStatus,
            String keyword
    );

    BookCopyResponse getBookCopyById(String copyId);

    BookCopyResponse createBookCopy(
            String bookId,
            String copyCode,
            String location,
            String note
    );

    BookCopyResponse updateBookCopy(
            String copyId,
            String copyCode,
            String location,
            BookCopyStatus copyStatus,
            String note
    );

    BookCopyResponse updateBookCopyStatus(String copyId, BookCopyStatus copyStatus);
}