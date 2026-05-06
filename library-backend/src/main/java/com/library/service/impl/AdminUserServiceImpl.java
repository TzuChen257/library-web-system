package com.library.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.library.entity.User;
import com.library.entity.enums.StatusType;
import com.library.exception.LibraryBusinessException;
import com.library.exception.ResponseCode;
import com.library.repository.UserRepository;
import com.library.service.AdminUserService;
import com.library.util.IdGenerator;
import com.library.util.security.LoginUserHolder;

@Service
@Transactional
public class AdminUserServiceImpl implements AdminUserService {

    private static final String ROLE_READER = "READER";
    private static final String ROLE_ADMIN = "ADMIN";
    
    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<User> getAdminUsers(String keyword,String role,StatusType status) {
        LoginUserHolder.requireAdmin();

        List<User> users;

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();

            users = userRepository.findByUsernameContainingOrNameContainingOrEmailContaining(
                    kw,
                    kw,
                    kw
            );
        } else if (StringUtils.hasText(role)) {
            users = userRepository.findByRole(role.trim().toUpperCase());
        } else if (status != null) {
            users = userRepository.findByStatus(status);
        } else {
            users = userRepository.findAll();
        }

        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        LoginUserHolder.requireAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.USER_NOT_FOUND));

        return user;
    }

    @Override
    public User createUser(User request) {
        LoginUserHolder.requireAdmin();

        validateCreateRequest(request);

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
        user.setEmail(trimToNull(request.getEmail()));
        user.setPhone(trimToNull(request.getPhone()));
        user.setRole(normalizeRole(request.getRole()));
        user.setStatus(request.getStatus() == null ? StatusType.ACTIVE : request.getStatus());

        User savedUser = userRepository.save(user);

        return savedUser;
    }

    @Override
    public User updateUser(String userId, User request) {
        LoginUserHolder.requireAdmin();

        validateUpdateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.USER_NOT_FOUND));

        if (StringUtils.hasText(request.getEmail())) {
            String email = request.getEmail().trim();

            if (userRepository.existsByEmailAndUserIdNot(email, userId)) {
                throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "Email 已被其他使用者使用");
            }

            user.setEmail(email);
        } else {
            user.setEmail(null);
        }

        user.setName(request.getName().trim());
        user.setPhone(trimToNull(request.getPhone()));

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(request.getPassword());
        }

        if (StringUtils.hasText(request.getRole())) {
            user.setRole(normalizeRole(request.getRole()));
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        User savedUser = userRepository.save(user);

        return savedUser;
    }

    @Override
    public User updateUserStatus(String userId, StatusType status) {
        LoginUserHolder.requireAdmin();

        if (status == null) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "使用者狀態不可為空");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.USER_NOT_FOUND));

        user.setStatus(status);

        User savedUser = userRepository.save(user);

        return savedUser;
    }
    
    @Override
    public User restoreBorrowPermission(String userId) {
        LoginUserHolder.requireAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LibraryBusinessException(ResponseCode.USER_NOT_FOUND));

        user.setBorrowSuspended(false);

        User savedUser = userRepository.save(user);

        return savedUser;
    }

    private void validateCreateRequest(User request) {
        if (request == null) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "使用者資料不可為空");
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

        normalizeRole(request.getRole());
    }

    private void validateUpdateRequest(User request) {
        if (request == null) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "使用者資料不可為空");
        }

        if (!StringUtils.hasText(request.getName())) {
            throw new LibraryBusinessException(ResponseCode.BAD_REQUEST, "姓名不可為空");
        }
    }

    private String normalizeRole(String role) {
        if (!StringUtils.hasText(role)) {
            return ROLE_READER;
        }

        String normalizedRole = role.trim().toUpperCase();

        if (!ROLE_READER.equals(normalizedRole) && !ROLE_ADMIN.equals(normalizedRole)) {
            throw new LibraryBusinessException(
                    ResponseCode.BAD_REQUEST,
                    "角色只能是 READER 或 ADMIN"
            );
        }

        return normalizedRole;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}