package com.company.auth.repository;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.company.auth.entity.Role;
import com.company.auth.entity.User;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldFindUserByUsername() {
        User user = User.builder()
                .username("test@example.com")
                .password("password")
                .role(Role.USER)
                .build();
        
        when(userRepository.findByUsername("test@example.com"))
                .thenReturn(Optional.of(user));

        var found = userRepository.findByUsername("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        var found = userRepository.findByUsername("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void shouldSaveUser() {
        User user = User.builder()
                .username("new@example.com")
                .password("pass")
                .role(Role.ADMIN)
                .build();
        
        User savedUser = User.builder()
                .id(1L)
                .username("new@example.com")
                .password("pass")
                .role(Role.ADMIN)
                .build();

        when(userRepository.save(user)).thenReturn(savedUser);
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("new@example.com");
    }
}
