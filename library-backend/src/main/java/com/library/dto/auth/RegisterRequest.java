package com.library.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    private String username;

    private String password;

    private String name;

    private String email;

    private String phone;
}