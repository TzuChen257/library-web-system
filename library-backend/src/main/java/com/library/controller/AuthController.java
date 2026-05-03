package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.auth.LoginRequest;
import com.library.dto.auth.LoginResponse;
import com.library.dto.common.ApiResponse;
import com.library.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	@Autowired
	private AuthService authService;
	
	@PostMapping("login")
	public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request){
		LoginResponse response=authService.login(request);
		return ApiResponse.success("登入成功",response);
	}
	/*
	@PostMapping("logout")
	public ApiResponse<?> logout(){
		return ApiResponse<?>(sucess,message);
	}
	*/
}
