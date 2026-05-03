package com.library.exception;

public class LibraryBusinessException extends RuntimeException{
	
	private final ResponseCode responseCode;

	public LibraryBusinessException(ResponseCode responseCode) {
		super(responseCode.getMessage());
		this.responseCode = responseCode;
	}
	
	public LibraryBusinessException(ResponseCode responseCode,String customMsg) {
		super(customMsg);
		this.responseCode = responseCode;
	}

	public ResponseCode getResponseCode() {
		return responseCode;
	}
	
}
