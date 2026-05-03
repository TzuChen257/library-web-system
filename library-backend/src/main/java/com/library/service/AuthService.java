package com.library.service;

import com.library.dto.auth.LoginRequest;
import com.library.dto.auth.LoginResponse;

public interface AuthService {
	LoginResponse login(LoginRequest request);
}
