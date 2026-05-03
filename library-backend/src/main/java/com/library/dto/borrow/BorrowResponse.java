package com.library.dto.borrow;

import java.time.LocalDate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BorrowResponse {
	private Long borrowId;
    private String bookId;
    private String title;
    private String copyId;
    private String copyCode;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private String borrowStatus;
}
