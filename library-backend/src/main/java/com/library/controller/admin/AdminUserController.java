package com.library.controller.admin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.dto.common.ApiResponse;
import com.library.entity.User;
import com.library.entity.enums.StatusType;
import com.library.service.AdminUserService;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
	
	@Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<List<User>> getAdminUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) StatusType status
    ) {
        return ApiResponse.success("查詢使用者清單成功", adminUserService.getAdminUsers(keyword, role, status));
    }

    @GetMapping("/{userId}")
    public ApiResponse<User> getUserById(@PathVariable String userId) {
        return ApiResponse.success("查詢使用者成功",adminUserService.getUserById(userId));
    }

    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User request) {
        return ApiResponse.success("新增使用者成功",adminUserService.createUser(request));
    }

    @PutMapping("/{userId}")
    public ApiResponse<User> updateUser(@PathVariable String userId,@RequestBody User request) {
        return ApiResponse.success("修改使用者成功",adminUserService.updateUser(userId, request));
    }

    @PatchMapping("/{userId}/status")
    public ApiResponse<User> updateUserStatus(@PathVariable String userId,@RequestParam StatusType status) {
        return ApiResponse.success("修改使用者狀態成功",adminUserService.updateUserStatus(userId, status));
    }
    
    @PatchMapping("/{userId}/restore-borrow-permission")
    public ApiResponse<User> restoreBorrowPermission(@PathVariable String userId) {
        return ApiResponse.success("已開通讀者借書功能",adminUserService.restoreBorrowPermission(userId));
    }
}