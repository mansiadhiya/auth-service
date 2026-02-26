package com.company.auth.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthRequestTest {

    @Test
    void shouldCreateAuthRequestWithBuilder() {
        AuthRequest request = AuthRequest.builder()
                .username("test@example.com")
                .password("password123")
                .build();

        assertThat(request.getUsername()).isEqualTo("test@example.com");
        assertThat(request.getPassword()).isEqualTo("password123");
    }
}
