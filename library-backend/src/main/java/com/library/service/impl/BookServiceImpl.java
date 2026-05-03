package com.library.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.book.BookCopyResponse;
import com.library.dto.book.BookDetailResponse;
import com.library.dto.book.BookListResponse;
import com.library.entity.Book;
import com.library.entity.BookCategory;
import com.library.entity.BookCopy;
import com.library.entity.enums.BookCopyStatus;
import com.library.entity.enums.StatusType;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.service.BookService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly=true)//僅查詢不做新增修改
public class BookServiceImpl implements BookService{

	private BookRepository bookRepository;
	private BookCopyRepository bookCopyRepository;
	
	@Override
	public List<BookListResponse> searchBooks(String keyword, String categoryId) {
		List<Book> books;
		if(keyword!=null&&!keyword.trim().isEmpty()) {
			String kw=keyword.trim();
			books=bookRepository.findByTitleContainingOrAuthorContainingOrIsbnContaining(kw, kw, kw);
		} else {
			books=bookRepository.findByStatus(StatusType.ACTIVE);
		}
		return books.stream()
				.filter(book->book.getStatus()==StatusType.ACTIVE)
				.filter(book->isCategoryMatched(book,categoryId))
				.map(this::toBookListResponse)
				.collect(Collectors.toList());
	}

	@Override
	public BookDetailResponse getBookDetail(String bookId) {
		Book book=bookRepository.findById(bookId)
				.orElseThrow(()->new LibraryBusinessException(ResponseCode.BOOK_NOT_FOUND));
		if(book.getStatus()!=StatusType.ACTIVE) {
			throw new LibraryBusinessException(ResponseCode.BOOK_NOT_FOUND);
		}
		
		BookDetailResponse response = new BookDetailResponse();
        response.setBookId(book.getBookId());
        response.setIsbn(book.getIsbn());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setPublisher(book.getPublisher());
        response.setPublishYear(book.getPublishYear());
        response.setDescription(book.getDescription());
        response.setCoverUrl(book.getCoverUrl());

        BookCategory category = book.getCategory();
        if (category != null) {
            response.setCategoryId(category.getCategoryId());
            response.setCategoryName(category.getCategoryName());
        }

        response.setTotalCopyCount(bookCopyRepository.countByBook_BookId(book.getBookId()));
        response.setAvailableCopyCount(bookCopyRepository
        		.countByBook_BookIdAndCopyStatus(book.getBookId(),BookCopyStatus.AVAILABLE));

        List<BookCopyResponse> copies = bookCopyRepository
                .findByBook_BookIdOrderByCopyIdAsc(book.getBookId())
                .stream().map(this::toBookCopyResponse).collect(Collectors.toList());

        response.setCopies(copies);
        return response;
	}
	
	private boolean isCategoryMatched(Book book,String categoryId) {
		if(categoryId==null||categoryId.trim().isEmpty()) {
			return true;
		}
		return book.getCategory()!=null&&book.getCategory().getCategoryId().equals(categoryId.trim());
	}
	
	private BookListResponse toBookListResponse(Book book) {
		BookListResponse response=new BookListResponse();
		response.setBookId(book.getBookId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setPublisher(book.getPublisher());
        response.setPublishYear(book.getPublishYear());
        response.setCoverUrl(book.getCoverUrl());
        BookCategory category=book.getCategory();
        if(category!=null) {
        	response.setCategoryId(category.getCategoryId());
        	response.setCategoryName(category.getCategoryName());
        }
        response.setTotalCopyCount(bookCopyRepository.countByBook_BookId(book.getBookId()));
        response.setAvailableCopyCount(bookCopyRepository
        		.countByBook_BookIdAndCopyStatus(book.getBookId(), BookCopyStatus.AVAILABLE));
		return response;
	}
	
	private BookCopyResponse toBookCopyResponse(BookCopy copy) {
		BookCopyResponse response=new BookCopyResponse();
		response.setCopyId(copy.getCopyId());
		response.setCopyCode(copy.getCopyCode());
		response.setCopyStatus(copy.getCopyStatus().name());
		response.setLocation(copy.getLocation());
		response.setNote(copy.getNote());
		return response;
	}

}
