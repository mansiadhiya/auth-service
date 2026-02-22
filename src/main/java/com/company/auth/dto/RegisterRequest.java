package com.company.auth.dto;

import com.company.auth.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}
