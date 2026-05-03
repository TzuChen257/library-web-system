package com.library.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.library.dto.common.ApiResponse;

@RestControllerAdvice//全域controller例外處理
public class GlobalExceptionHandler {
	
	@ExceptionHandler(LibraryBusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(LibraryBusinessException e) {
        ResponseCode responseCode = e.getResponseCode();
        return ResponseEntity
                .status(responseCode.getHttpStatus())
                .body(ApiResponse.fail(responseCode, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(ResponseCode.SYSTEM_ERROR.getHttpStatus())
                .body(ApiResponse.fail(ResponseCode.SYSTEM_ERROR));
    }
}

/*	
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)//一律回傳500
	public ApiResponse<Void> handleException(Exception e){
		return ApiResponse.fail(ResponseCode.SYSTEM_ERROR);
	}

*/