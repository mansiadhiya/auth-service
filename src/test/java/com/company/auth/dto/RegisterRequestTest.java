package com.company.auth.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.company.auth.entity.Role;

class RegisterRequestTest {

    @Test
    void shouldCreateRegisterRequestWithBuilder() {
        RegisterRequest request = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        assertThat(request.getUsername()).isEqualTo("test@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
        assertThat(request.getRole()).isEqualTo(Role.USER);
    }
}
