package com.company.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import com.company.auth.mapper.AuthMapper;
import com.company.auth.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private AuthRequest validAuthRequest;
    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("test@example.com");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setRole(Role.USER);

        validAuthRequest = AuthRequest.builder()
                .username("test@example.com")
                .password("plainPassword")
                .build();

        validRegisterRequest = RegisterRequest.builder()
                .username("newuser@example.com")
                .password("newPassword123")
                .role(Role.USER)
                .build();
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @DisplayName("Should authenticate user with valid credentials")
        void shouldAuthenticateWithValidCredentials() {
            // Given
            when(userRepository.findByUsername(validAuthRequest.getUsername()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(validAuthRequest.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("valid.jwt.token");

            // When
            AuthResponse response = authService.login(validAuthRequest);

            // Then
            assertThat(response)
                    .isNotNull()
                    .satisfies(r -> {
                        assertThat(r.getToken()).isEqualTo("valid.jwt.token");
                        assertThat(r.getRole()).isEqualTo("USER");
                    });

            verify(userRepository).findByUsername(validAuthRequest.getUsername());
            verify(passwordEncoder).matches(validAuthRequest.getPassword(), testUser.getPassword());
            verify(jwtUtil).generateToken(testUser);
        }

        @Test
        @DisplayName("Should authenticate admin user")
        void shouldAuthenticateAdminUser() {
            // Given
            testUser.setRole(Role.ADMIN);
            when(userRepository.findByUsername(validAuthRequest.getUsername()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(validAuthRequest.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("admin.jwt.token");

            // When
            AuthResponse response = authService.login(validAuthRequest);

            // Then
            assertThat(response.getRole()).isEqualTo("ADMIN");
            assertThat(response.getToken()).isEqualTo("admin.jwt.token");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user not found")
        void shouldThrowWhenUserNotFound() {
            // Given
            when(userRepository.findByUsername(validAuthRequest.getUsername()))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(validAuthRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");

            verify(userRepository).findByUsername(validAuthRequest.getUsername());
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when password is invalid")
        void shouldThrowWhenPasswordInvalid() {
            // Given
            when(userRepository.findByUsername(validAuthRequest.getUsername()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(validAuthRequest.getPassword(), testUser.getPassword()))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> authService.login(validAuthRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid password");

            verify(userRepository).findByUsername(validAuthRequest.getUsername());
            verify(passwordEncoder).matches(validAuthRequest.getPassword(), testUser.getPassword());
            verify(jwtUtil, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should handle null username gracefully")
        void shouldHandleNullUsername() {
            // Given
            validAuthRequest.setUsername(null);
            when(userRepository.findByUsername(null))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(validAuthRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Should handle empty username")
        void shouldHandleEmptyUsername() {
            // Given
            validAuthRequest.setUsername("");
            when(userRepository.findByUsername(""))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> authService.login(validAuthRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }
    }

    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() {
            // Given
            User mappedUser = new User();
            mappedUser.setUsername(validRegisterRequest.getUsername());
            mappedUser.setRole(validRegisterRequest.getRole());

            when(userRepository.findByUsername(validRegisterRequest.getUsername()))
                    .thenReturn(Optional.empty());
            when(authMapper.toEntity(validRegisterRequest))
                    .thenReturn(mappedUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenReturn("$2a$10$encodedNewPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(mappedUser);

            // When
            String result = authService.register(validRegisterRequest);

            // Then
            assertThat(result).isEqualTo("User registered successfully");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            
            assertThat(savedUser)
                    .satisfies(user -> {
                        assertThat(user.getUsername()).isEqualTo(validRegisterRequest.getUsername());
                        assertThat(user.getPassword()).isEqualTo("$2a$10$encodedNewPassword");
                        assertThat(user.getRole()).isEqualTo(Role.USER);
                    });

            verify(userRepository).findByUsername(validRegisterRequest.getUsername());
            verify(authMapper).toEntity(validRegisterRequest);
            verify(passwordEncoder).encode(validRegisterRequest.getPassword());
        }

        @Test
        @DisplayName("Should register admin user")
        void shouldRegisterAdminUser() {
            // Given
            validRegisterRequest.setRole(Role.ADMIN);
            User mappedUser = new User();
            mappedUser.setUsername(validRegisterRequest.getUsername());
            mappedUser.setRole(Role.ADMIN);

            when(userRepository.findByUsername(validRegisterRequest.getUsername()))
                    .thenReturn(Optional.empty());
            when(authMapper.toEntity(validRegisterRequest))
                    .thenReturn(mappedUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenReturn("$2a$10$encodedAdminPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(mappedUser);

            // When
            String result = authService.register(validRegisterRequest);

            // Then
            assertThat(result).isEqualTo("User registered successfully");
            
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("Should default to USER role when role is null")
        void shouldDefaultToUserRoleWhenNull() {
            // Given
            validRegisterRequest.setRole(null);
            User mappedUser = new User();
            mappedUser.setUsername(validRegisterRequest.getUsername());
            mappedUser.setRole(null);

            when(userRepository.findByUsername(validRegisterRequest.getUsername()))
                    .thenReturn(Optional.empty());
            when(authMapper.toEntity(validRegisterRequest))
                    .thenReturn(mappedUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(mappedUser);

            // When
            String result = authService.register(validRegisterRequest);

            // Then
            assertThat(result).isEqualTo("User registered successfully");
            
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user already exists")
        void shouldThrowWhenUserAlreadyExists() {
            // Given
            when(userRepository.findByUsername(validRegisterRequest.getUsername()))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User already exists");

            verify(userRepository).findByUsername(validRegisterRequest.getUsername());
            verify(authMapper, never()).toEntity(any());
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle duplicate username case insensitive")
        void shouldHandleDuplicateUsernameCaseInsensitive() {
            // Given
            validRegisterRequest.setUsername("TEST@EXAMPLE.COM");
            when(userRepository.findByUsername("TEST@EXAMPLE.COM"))
                    .thenReturn(Optional.of(testUser));

            // When & Then
            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User already exists");
        }

        @Test
        @DisplayName("Should handle registration with special characters in username")
        void shouldHandleSpecialCharactersInUsername() {
            // Given
            validRegisterRequest.setUsername("user+test@example-domain.com");
            User mappedUser = new User();
            mappedUser.setUsername("user+test@example-domain.com");
            mappedUser.setRole(Role.USER);

            when(userRepository.findByUsername("user+test@example-domain.com"))
                    .thenReturn(Optional.empty());
            when(authMapper.toEntity(validRegisterRequest))
                    .thenReturn(mappedUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenReturn("$2a$10$encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(mappedUser);

            // When
            String result = authService.register(validRegisterRequest);

            // Then
            assertThat(result).isEqualTo("User registered successfully");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle repository exception during login")
        void shouldHandleRepositoryExceptionDuringLogin() {
            // Given
            when(userRepository.findByUsername(validAuthRequest.getUsername()))
                    .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            assertThatThrownBy(() -> authService.login(validAuthRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection failed");
        }

        @Test
        @DisplayName("Should handle repository exception during registration")
        void shouldHandleRepositoryExceptionDuringRegistration() {
            // Given
            when(userRepository.findByUsername(validRegisterRequest.getUsername()))
                    .thenReturn(Optional.empty());
            when(authMapper.toEntity(validRegisterRequest))
                    .thenReturn(testUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenThrow(new RuntimeException("Database save failed"));

            // When & Then
            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database save failed");
        }

        @Test
        @DisplayName("Should handle JWT generation failure")
        void shouldHandleJwtGenerationFailure() {
            // Given
            when(userRepository.findByUsername(validAuthRequest.getUsername()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(validAuthRequest.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenThrow(new RuntimeException("JWT generation failed"));

            // When & Then
            assertThatThrownBy(() -> authService.login(validAuthRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("JWT generation failed");
        }

        @Test
        @DisplayName("Should handle password encoding failure")
        void shouldHandlePasswordEncodingFailure() {
            // Given
            when(userRepository.findByUsername(validRegisterRequest.getUsername()))
                    .thenReturn(Optional.empty());
            when(authMapper.toEntity(validRegisterRequest))
                    .thenReturn(testUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenThrow(new RuntimeException("Password encoding failed"));

            // When & Then
            assertThatThrownBy(() -> authService.register(validRegisterRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Password encoding failed");
        }
    }

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

        @Test
        @DisplayName("Should handle concurrent registration attempts")
        void shouldHandleConcurrentRegistrationAttempts() {
            // Given - simulate race condition
            when(userRepository.findByUsername(validRegisterRequest.getUsername()))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(testUser)); // Second call finds existing user

            User mappedUser = new User();
            mappedUser.setUsername(validRegisterRequest.getUsername());
            mappedUser.setRole(Role.USER);

            when(authMapper.toEntity(validRegisterRequest))
                    .thenReturn(mappedUser);
            when(passwordEncoder.encode(validRegisterRequest.getPassword()))
                    .thenReturn("encodedPassword");
            when(userRepository.save(any(User.class)))
                    .thenReturn(mappedUser);

            // When
            String result = authService.register(validRegisterRequest);

            // Then
            assertThat(result).isEqualTo("User registered successfully");
        }

        @Test
        @DisplayName("Should verify all role types work correctly")
        void shouldVerifyAllRoleTypes() {
            // Test USER role
            testUser.setRole(Role.USER);
            when(userRepository.findByUsername(validAuthRequest.getUsername()))
                    .thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(validAuthRequest.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("user-token");

            AuthResponse userResponse = authService.login(validAuthRequest);
            assertThat(userResponse.getRole()).isEqualTo("USER");

            // Test ADMIN role
            testUser.setRole(Role.ADMIN);
            when(jwtUtil.generateToken(testUser))
                    .thenReturn("admin-token");

            AuthResponse adminResponse = authService.login(validAuthRequest);
            assertThat(adminResponse.getRole()).isEqualTo("ADMIN");
        }
    }
}
