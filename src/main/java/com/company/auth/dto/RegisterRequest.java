package com.company.auth.dto;

import com.company.auth.entity.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}
