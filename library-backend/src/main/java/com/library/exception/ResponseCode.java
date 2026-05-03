package com.library.exception;

import org.springframework.http.HttpStatus;

public enum ResponseCode {

    SUCCESS("SUCCESS", "操作成功", HttpStatus.OK),
    // 請求相關
    BAD_REQUEST("BAD_REQUEST", "請求資料格式錯誤", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "尚未登入或登入已失效", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "沒有操作權限", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "查無資料", HttpStatus.NOT_FOUND),
    // 使用者相關
    USER_NOT_FOUND("USER_NOT_FOUND", "找不到使用者", HttpStatus.NOT_FOUND),
    USER_DISABLED("USER_DISABLED", "使用者帳號已停用", HttpStatus.FORBIDDEN),
    LOGIN_FAILED("LOGIN_FAILED", "帳號或密碼錯誤", HttpStatus.UNAUTHORIZED),
    // 書籍相關
    BOOK_NOT_FOUND("BOOK_NOT_FOUND", "找不到書籍資料", HttpStatus.NOT_FOUND),
    BOOK_COPY_NOT_FOUND("BOOK_COPY_NOT_FOUND", "找不到館藏資料", HttpStatus.NOT_FOUND),
    BOOK_COPY_NOT_AVAILABLE("BOOK_COPY_NOT_AVAILABLE", "目前沒有可借閱的館藏", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "查無此分類", HttpStatus.NOT_FOUND),
    // 借閱相關
    BORROW_LIMIT_EXCEEDED("BORROW_LIMIT_EXCEEDED", "已達借閱上限", HttpStatus.CONFLICT),
    BORROW_RECORD_NOT_FOUND("BORROW_RECORD_NOT_FOUND", "找不到借閱紀錄", HttpStatus.NOT_FOUND),
    BORROW_RECORD_NOT_BELONG_TO_USER("BORROW_RECORD_NOT_BELONG_TO_USER", "此借閱紀錄不屬於目前使用者", HttpStatus.FORBIDDEN),
    BORROW_STATUS_INVALID("BORROW_STATUS_INVALID", "借閱狀態不允許此操作", HttpStatus.CONFLICT),
    // 預約相關
    RESERVATION_DUPLICATED("RESERVATION_DUPLICATED", "已存在尚未完成的預約", HttpStatus.CONFLICT),
    RESERVATION_NOT_FOUND("RESERVATION_NOT_FOUND", "找不到預約紀錄", HttpStatus.NOT_FOUND),
    // 訊息相關
    MESSAGE_NOT_FOUND("MESSAGE_NOT_FOUND", "找不到訊息", HttpStatus.NOT_FOUND),
    MESSAGE_NOT_BELONG_TO_USER("MESSAGE_NOT_BELONG_TO_USER", "此訊息不屬於目前使用者", HttpStatus.FORBIDDEN),
    // 其他
    SYSTEM_ERROR("SYSTEM_ERROR", "系統發生錯誤，請稍後再試", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
	private ResponseCode(String code, String message, HttpStatus httpStatus) {
		this.code = code;
		this.message = message;
		this.httpStatus = httpStatus;
	}
	public String getCode() {
		return code;
	}
	public String getMessage() {
		return message;
	}
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
