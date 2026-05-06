package com.library.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.library.dto.book.BookListResponse;
import com.library.dto.book.BookRequest;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.StatusType;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BookCategoryRepository;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.service.AdminBookService;
import com.library.util.IdGenerator;
import com.library.util.security.LoginUserHolder;

import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class AdminBookServiceImpl implements AdminBookService {

    private BookRepository bookRepository;
    private BookCategoryRepository bookCategoryRepository;
    private BookCopyRepository bookCopyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookListResponse> getAdminBooks(String keyword, StatusType status) {
        LoginUserHolder.requireAdmin();

        List<Book> books;

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            books = bookRepository.findByTitleContainingOrAuthorContainingOrIsbnContaining(
                    kw,
                    kw,
                    kw
            );
        } else if (status != null) {
            books = bookRepository.findByStatus(status);
        } else {
            books = bookRepository.findAll();
        }

        return books.stream()
                .map(this::toBookListResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BookListResponse createBook(BookRequest request) {
        LoginUserHolder.requireAdmin();

        validateBookRequest(request);

        BookCategory category = bookCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.CATEGORY_NOT_FOUND));

        Book book = new Book();

        book.setBookId(IdGenerator.generateBookId());
        book.setCategory(category);
        book.setIsbn(trimToNull(request.getIsbn()));
        book.setTitle(request.getTitle().trim());
        book.setAuthor(trimToNull(request.getAuthor()));
        book.setPublisher(trimToNull(request.getPublisher()));
        book.setPublishYear(request.getPublishYear());
        book.setDescription(trimToNull(request.getDescription()));
        book.setCoverUrl(trimToNull(request.getCoverUrl()));
        book.setStatus(StatusType.ACTIVE);

        Book savedBook = bookRepository.save(book);

        return toBookListResponse(savedBook);
    }

    @Override
    public BookListResponse updateBook(String bookId, BookRequest request) {
        LoginUserHolder.requireAdmin();

        validateBookRequest(request);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BOOK_NOT_FOUND));

        BookCategory category = bookCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.CATEGORY_NOT_FOUND));

        book.setCategory(category);
        book.setIsbn(trimToNull(request.getIsbn()));
        book.setTitle(request.getTitle().trim());
        book.setAuthor(trimToNull(request.getAuthor()));
        book.setPublisher(trimToNull(request.getPublisher()));
        book.setPublishYear(request.getPublishYear());
        book.setDescription(trimToNull(request.getDescription()));
        book.setCoverUrl(trimToNull(request.getCoverUrl()));

        Book savedBook = bookRepository.save(book);

        return toBookListResponse(savedBook);
    }

    @Override
    public BookListResponse updateBookStatus(String bookId, StatusType status) {
        LoginUserHolder.requireAdmin();

        if (status == null) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "書目狀態不可為空");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.BOOK_NOT_FOUND));

        book.setStatus(status);

        Book savedBook = bookRepository.save(book);

        return toBookListResponse(savedBook);
    }

    private void validateBookRequest(BookRequest request) {
        if (request == null) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "書目資料不可為空");
        }

        if (!StringUtils.hasText(request.getCategoryId())) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "分類不可為空");
        }

        if (!StringUtils.hasText(request.getTitle())) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "書名不可為空");
        }
    }

    private BookListResponse toBookListResponse(Book book) {
        BookListResponse response = new BookListResponse();

        response.setBookId(book.getBookId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setPublisher(book.getPublisher());
        response.setPublishYear(book.getPublishYear());
        response.setCoverUrl(book.getCoverUrl());
        response.setIsbn(book.getIsbn());
        response.setStatus(book.getStatus() == null ? null : book.getStatus().name());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());

        if (book.getCategory() != null) {
            response.setCategoryId(book.getCategory().getCategoryId());
            response.setCategoryName(book.getCategory().getCategoryName());
        }

        response.setTotalCopyCount(
                bookCopyRepository.countByBook_BookId(book.getBookId())
        );

        response.setAvailableCopyCount(
                bookCopyRepository.countByBook_BookIdAndCopyStatus(
                        book.getBookId(),
                        BookCopyStatus.AVAILABLE
                )
        );

        return response;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}