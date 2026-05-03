package com.library.dto.common;

import com.library.exception.ResponseCode;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor//讓前端new ApiResponse物件
@Data
public class ApiResponse<T> {
	private boolean success;
	private String code;
	private String message;
	private T data;//適用於不同entity或資料型態
	
	//因為success()是static方法，自己要宣告一次<T>
	public static<T> ApiResponse<T> success(String message,T data){
		return new ApiResponse<>(true,ResponseCode.SUCCESS.getCode(),message,data);
	}
	public static<T> ApiResponse<T> success(String message){
		return new ApiResponse<>(true,ResponseCode.SUCCESS.getCode(),message,null);
	}
	
	public static<T> ApiResponse<T> fail(ResponseCode error){
		return new ApiResponse<>(false,error.getCode(),error.getMessage(),null);
	}
	public static<T> ApiResponse<T> fail(ResponseCode error,String customMsg){
		return new ApiResponse<>(false,error.getCode(),customMsg,null);
	}
}