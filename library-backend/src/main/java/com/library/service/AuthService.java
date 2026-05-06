package com.library.service;

import com.library.dto.auth.LoginRequest;
import com.library.dto.auth.LoginResponse;
import com.library.dto.auth.RegisterRequest;
import com.library.dto.user.UserResponse;

public interface AuthService {
	LoginResponse login(LoginRequest request);
	UserResponse registerReader(RegisterRequest request);
	void logout();
}
