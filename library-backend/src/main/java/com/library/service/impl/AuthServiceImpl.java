package com.library.service.impl;

import org.springframework.stereotype.Service;

import com.library.dto.auth.LoginRequest;
import com.library.dto.auth.LoginResponse;
import com.library.entity.User;
import com.library.entity.enums.StatusType;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.UserRepository;
import com.library.service.AuthService;
import com.library.util.security.JwtUtil;

@Service
public class AuthServiceImpl implements AuthService{

	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	
	public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
		super();
		this.userRepository = userRepository;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		User user=userRepository.findByUsername(request.getUsername())
				.orElseThrow(()->new LibraryBusinessException(ResponseCode.LOGIN_FAILED));
		if(!user.getPassword().equals(request.getPassword())) {
			throw new LibraryBusinessException(ResponseCode.LOGIN_FAILED);
		}
		if(!user.getStatus().equals(StatusType.ACTIVE)) {
			throw new LibraryBusinessException(ResponseCode.USER_DISABLED);
		}
		String token=jwtUtil.generateToken(user.getUserId(), user.getName(), user.getRole());
		return new LoginResponse(token,user.getUserId(), user.getUsername(), user.getName(), user.getRole());
	}

}
