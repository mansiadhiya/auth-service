package com.company.auth.mapper;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.company.auth.dto.RegisterRequest;
import com.company.auth.entity.Role;
import com.company.auth.entity.User;

class AuthMapperTest {

    private final AuthMapper mapper = Mappers.getMapper(AuthMapper.class);

    @Test
    void shouldMapRegisterRequestToUser() {
        RegisterRequest request = RegisterRequest.builder()
                .username("test@example.com")
                .password("password123")
                .role(Role.USER)
                .build();

        User user = mapper.toEntity(request);

        assertThat(user.getUsername()).isEqualTo("test@example.com");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getId()).isNull();
        assertThat(user.getPassword()).isNull();
    }

    @Test
    void shouldMapWithAdminRole() {
        RegisterRequest request = RegisterRequest.builder()
                .username("admin@example.com")
                .password("admin123")
                .role(Role.ADMIN)
                .build();

        User user = mapper.toEntity(request);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }
}
