package com.company.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.company.auth.dto.AuthRequest;
import com.company.auth.dto.AuthResponse;
import com.company.auth.dto.JwtUtil;
import com.company.auth.dto.RegisterRequest;
import com.company.auth.entity.Role;
import com.company.auth.entity.User;
import com.company.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository repo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService service;

    private User user;

    @BeforeEach
    void init() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.USER);
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            AuthRequest request = new AuthRequest();
            request.setUsername("testuser");
            request.setPassword("password123");

            when(repo.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(encoder.matches("password123", "encodedPassword")).thenReturn(true);
            when(jwtUtil.generateToken(user)).thenReturn("jwt-token");

            AuthResponse response = service.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getRole()).isEqualTo("USER");
            verify(repo).findByUsername("testuser");
            verify(encoder).matches("password123", "encodedPassword");
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowWhenUserNotFound() {
            AuthRequest request = new AuthRequest();
            request.setUsername("unknown");
            request.setPassword("password123");

            when(repo.findByUsername("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.login(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");
            verify(encoder, never()).matches(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when password is invalid")
        void shouldThrowWhenPasswordInvalid() {
            AuthRequest request = new AuthRequest();
            request.setUsername("testuser");
            request.setPassword("wrongpassword");

            when(repo.findByUsername("testuser")).thenReturn(Optional.of(user));
            when(encoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

            assertThatThrownBy(() -> service.login(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid password");
            verify(jwtUtil, never()).generateToken(any());
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterSuccessfully() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setPassword("password123");
            request.setRole(Role.USER);

            when(repo.findByUsername("newuser")).thenReturn(Optional.empty());
            when(encoder.encode("password123")).thenReturn("encodedPassword");
            when(repo.save(any(User.class))).thenReturn(user);

            String result = service.register(request);

            assertThat(result).isEqualTo("User registered successfully");
            verify(repo).findByUsername("newuser");
            verify(encoder).encode("password123");
            verify(repo).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when user already exists")
        void shouldThrowWhenUserExists() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("testuser");
            request.setPassword("password123");

            when(repo.findByUsername("testuser")).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.register(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User already exists");
            verify(repo, never()).save(any());
        }

        @Test
        @DisplayName("Should default to USER role when role not specified")
        void shouldDefaultToUserRole() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setPassword("password123");
            request.setRole(null);

            when(repo.findByUsername("newuser")).thenReturn(Optional.empty());
            when(encoder.encode("password123")).thenReturn("encodedPassword");
            when(repo.save(any(User.class))).thenReturn(user);

            String result = service.register(request);

            assertThat(result).isEqualTo("User registered successfully");
            verify(repo).save(argThat(u -> u.getRole() == Role.USER));
        }
    }
}
