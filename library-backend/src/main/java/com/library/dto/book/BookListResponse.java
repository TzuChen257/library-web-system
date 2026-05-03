package com.library.dto.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookListResponse {
	private String bookId;
    private String title;
    private String author;
    private String publisher;
    private Integer publishYear;
    private String categoryId;
    private String categoryName;
    private String coverUrl;
    private long totalCopyCount;
    private long availableCopyCount;
}
