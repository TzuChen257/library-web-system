package com.library.dto.book;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookListResponse {
	//書目查詢用
	private String bookId;
    private String categoryId;
    private String categoryName;

    private String title;
    private String author;
    private String publisher;
    private Integer publishYear;
    private String coverUrl;
    
    private long totalCopyCount;
    private long availableCopyCount;
    //館藏編輯用
    private String isbn;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
