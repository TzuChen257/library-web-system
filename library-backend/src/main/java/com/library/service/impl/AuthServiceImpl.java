package com.library.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.library.dto.auth.LoginRequest;
import com.library.dto.auth.LoginResponse;
import com.library.dto.auth.RegisterRequest;
import com.library.dto.user.UserResponse;
import com.library.entity.User;
import com.library.entity.enums.StatusType;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.UserRepository;
import com.library.service.AuthService;
import com.library.util.IdGenerator;
import com.library.util.security.JwtUtil;
import com.library.util.security.LoginUserHolder;

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
	
	@Override
	public UserResponse registerReader(RegisterRequest request) {
	    validateRegisterRequest(request);

	    String username = request.getUsername().trim();

	    if (userRepository.existsByUsername(username)) {
	        throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "帳號已存在");
	    }

	    if (StringUtils.hasText(request.getEmail())
	            && userRepository.existsByEmail(request.getEmail().trim())) {
	        throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "Email 已被使用");
	    }

	    User user = new User();

	    user.setUserId(IdGenerator.generateUserId());
	    user.setUsername(username);
	    user.setPassword(request.getPassword());
	    user.setName(request.getName().trim());
	    user.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
	    user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
	    user.setRole("READER");
	    user.setStatus(StatusType.ACTIVE);
	    user.setBorrowSuspended(false);

	    User savedUser = userRepository.save(user);

	    return toUserResponse(savedUser);
	}
	
	@Override
	public void logout() {
	    LoginUserHolder.requireLoginUser();
	}

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "註冊資料不可為空");
        }

        if (!StringUtils.hasText(request.getUsername())) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "帳號不可為空");
        }

        if (!StringUtils.hasText(request.getPassword())) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "密碼不可為空");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "姓名不可為空");
        }
    }
    
    private UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();

        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setBorrowSuspended(user.getBorrowSuspended());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        return response;
    }

}
