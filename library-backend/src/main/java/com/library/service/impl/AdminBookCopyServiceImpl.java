package com.library.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.library.dto.book.BookCopyResponse;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.enums.BookCopyStatus;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.service.AdminBookCopyService;
import com.library.util.IdGenerator;
import com.library.util.security.LoginUserHolder;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class AdminBookCopyServiceImpl implements AdminBookCopyService {

    private BookCopyRepository bookCopyRepository;
    private BookRepository bookRepository;


    @Override
    @Transactional(readOnly = true)
    public List<BookCopyResponse> getAdminBookCopies(
            String bookId,
            BookCopyStatus copyStatus,
            String keyword
    ) {
        LoginUserHolder.requireAdmin();

        List<BookCopy> copies;

        if (StringUtils.hasText(bookId) && copyStatus != null) {
            copies = bookCopyRepository.findByBook_BookIdAndCopyStatus(
                    bookId.trim(),
                    copyStatus
            );
        } else if (StringUtils.hasText(bookId)) {
            copies = bookCopyRepository.findByBook_BookId(bookId.trim());
        } else if (copyStatus != null) {
            copies = bookCopyRepository.findByCopyStatus(copyStatus);
        } else if (StringUtils.hasText(keyword)) {
            copies = bookCopyRepository.findByCopyCodeContaining(keyword.trim());
        } else {
            copies = bookCopyRepository.findAll();
        }

        return copies.stream()
                .map(this::toBookCopyResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookCopyResponse getBookCopyById(String copyId) {
        LoginUserHolder.requireAdmin();

        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BOOK_COPY_NOT_FOUND));

        return toBookCopyResponse(copy);
    }

    @Override
    public BookCopyResponse createBookCopy(
            String bookId,
            String copyCode,
            String location,
            String note
    ) {
        LoginUserHolder.requireAdmin();

        if (!StringUtils.hasText(copyCode)) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "館藏條碼不可為空");
        }

        String code = copyCode.trim();

        if (bookCopyRepository.existsByCopyCode(code)) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "館藏條碼已存在");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BOOK_NOT_FOUND));

        BookCopy copy = new BookCopy();

        copy.setCopyId(IdGenerator.generateBookCopyId());
        copy.setBook(book);
        copy.setCopyCode(code);
        copy.setLocation(trimToNull(location));
        copy.setCopyStatus(BookCopyStatus.AVAILABLE);
        copy.setNote(trimToNull(note));

        BookCopy savedCopy = bookCopyRepository.save(copy);

        return toBookCopyResponse(savedCopy);
    }

    @Override
    public BookCopyResponse updateBookCopy(
            String copyId,
            String copyCode,
            String location,
            BookCopyStatus copyStatus,
            String note
    ) {
        LoginUserHolder.requireAdmin();

        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BOOK_COPY_NOT_FOUND));

        if (StringUtils.hasText(copyCode)) {
            String code = copyCode.trim();

            if (bookCopyRepository.existsByCopyCodeAndCopyIdNot(code, copyId)) {
                throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "館藏條碼已存在");
            }

            copy.setCopyCode(code);
        }

        copy.setLocation(trimToNull(location));

        if (copyStatus != null) {
            copy.setCopyStatus(copyStatus);
        }

        copy.setNote(trimToNull(note));

        BookCopy savedCopy = bookCopyRepository.save(copy);

        return toBookCopyResponse(savedCopy);
    }

    @Override
    public BookCopyResponse updateBookCopyStatus(String copyId, BookCopyStatus copyStatus) {
        LoginUserHolder.requireAdmin();

        if (copyStatus == null) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "館藏狀態不可為空");
        }

        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BOOK_COPY_NOT_FOUND));

        copy.setCopyStatus(copyStatus);

        BookCopy savedCopy = bookCopyRepository.save(copy);

        return toBookCopyResponse(savedCopy);
    }

    private BookCopyResponse toBookCopyResponse(BookCopy copy) {
        BookCopyResponse response = new BookCopyResponse();

        response.setCopyId(copy.getCopyId());
        response.setCopyCode(copy.getCopyCode());
        response.setLocation(copy.getLocation());
        response.setCopyStatus(copy.getCopyStatus() == null ? null : copy.getCopyStatus().name());
        response.setNote(copy.getNote());
        response.setCreatedAt(copy.getCreatedAt());
        response.setUpdatedAt(copy.getUpdatedAt());

        if (copy.getBook() != null) {
            response.setBookId(copy.getBook().getBookId());
            response.setTitle(copy.getBook().getTitle());
        }

        return response;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}