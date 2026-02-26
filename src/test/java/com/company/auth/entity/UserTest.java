package com.company.auth.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void shouldCreateUserWithBuilder() {
        User user = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void shouldSetAndGetProperties() {
        User user = new User();
        user.setId(2L);
        user.setUsername("admin@example.com");
        user.setPassword("adminpass");
        user.setRole(Role.ADMIN);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getUsername()).isEqualTo("admin@example.com");
        assertThat(user.getPassword()).isEqualTo("adminpass");
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void shouldCreateUserWithAllArgsConstructor() {
        User user = new User(1L, "user@test.com", "pass", Role.USER);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("user@test.com");
        assertThat(user.getPassword()).isEqualTo("pass");
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }
}
