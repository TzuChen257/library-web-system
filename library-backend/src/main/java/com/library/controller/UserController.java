package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.dto.user.UserResponse;
import com.library.entity.User;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.UserRepository;
import com.library.util.security.LoginUserHolder;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
    private UserRepository userRepository;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe() {
        String userId = LoginUserHolder.requireLoginUser().getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.USER_NOT_FOUND));

        return ApiResponse.success("查詢個人資料成功", toUserResponse(user));
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