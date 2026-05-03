package com.library.dto.book;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookDetailResponse {
	private String bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publishYear;
    private String description;
    private String coverUrl;
    private String categoryId;
    private String categoryName;
    private long totalCopyCount;
    private long availableCopyCount;
    private List<BookCopyResponse> copies;
}
