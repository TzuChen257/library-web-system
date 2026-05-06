package com.library.dto.reservation;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationResponse {
	private Long reservationId;

    private String userId;
    private String username;
    private String name;

    private String bookId;
    private String title;
    private String author;

    private LocalDateTime reservationDate;
    private LocalDateTime expireDate;

    private String reservationStatus;
    private Integer queueOrder;
    
    // 管理員預約管理頁輔助欄位
    private Integer availableCopyCount;
    private Boolean firstInQueue;
    private Boolean canNotify;
}
