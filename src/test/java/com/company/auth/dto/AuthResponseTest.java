package com.company.auth.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuthResponseTest {

    @Test
    void shouldCreateAuthResponse() {
        AuthResponse response = new AuthResponse("token123", "USER");

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void shouldCreateAuthResponseWithAdminRole() {
        AuthResponse response = new AuthResponse("adminToken", "ADMIN");

        assertThat(response.getToken()).isEqualTo("adminToken");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }
}
