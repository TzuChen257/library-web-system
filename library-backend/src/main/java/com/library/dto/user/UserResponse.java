package com.library.dto.user;

import java.time.LocalDateTime;

import com.library.entity.enums.StatusType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserResponse {

    private String userId;

    private String username;

    private String name;

    private String email;

    private String phone;

    private String role;

    private StatusType status;

    private Boolean borrowSuspended;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}