package com.library.dto.book;

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
}
