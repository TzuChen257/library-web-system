package com.library.dto.message;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MessageResponse {
	private Long messageId;
    private String title;
    private String content;
    private String messageType;
    private Boolean isRead;
    private Long relatedBorrowId;
    private Long relatedReservationId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
