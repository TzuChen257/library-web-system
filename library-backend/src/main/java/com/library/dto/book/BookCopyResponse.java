package com.library.dto.book;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BookCopyResponse {
	private String copyId;
    private String copyCode;
    private String location;
    private String copyStatus;
    private String note;
    //管理員功能使用
    private String bookId;
    private String title;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
