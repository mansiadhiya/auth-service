package com.company.auth.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.company.auth.entity.Role;
import com.company.auth.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKeyForTestingPurposesOnly1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);

        testUser = User.builder()
                .id(1L)
                .username("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .build();
    }

    @Test
    void shouldGenerateValidToken() {
        String token = jwtUtil.generateToken(testUser);
        
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        String token = jwtUtil.generateToken(testUser);
        
        Claims claims = jwtUtil.validateToken(token);
        
        assertThat(claims.getSubject()).isEqualTo("test@example.com");
        assertThat(claims.get("role")).isEqualTo("USER");
    }

    @Test
    void shouldGenerateTokenWithAdminRole() {
        testUser.setRole(Role.ADMIN);
        
        String token = jwtUtil.generateToken(testUser);
        Claims claims = jwtUtil.validateToken(token);
        
        assertThat(claims.get("role")).isEqualTo("ADMIN");
    }

    @Test
    void shouldThrowExceptionForInvalidToken() {
        assertThatThrownBy(() -> jwtUtil.validateToken("invalid.token.here"))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void shouldThrowExceptionForExpiredToken() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String expiredToken = jwtUtil.generateToken(testUser);
        
        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
