package com.library.service;

import java.util.List;

import com.library.entity.User;
import com.library.entity.enums.StatusType;

public interface AdminUserService {

    List<User> getAdminUsers(String keyword,String role,StatusType status);
    User getUserById(String userId);
    User createUser(User request);
    User updateUser(String userId, User request);
    User updateUserStatus(String userId, StatusType status);
    User restoreBorrowPermission(String userId);
}