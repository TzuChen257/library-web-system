package com.library.dto.book;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookRequest {
	private String categoryId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Integer publishYear;
    private String description;
    private String coverUrl;
}
