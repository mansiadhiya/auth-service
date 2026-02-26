package com.company.auth.entity;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void shouldHaveAdminRole() {
        assertThat(Role.ADMIN).isNotNull();
        assertThat(Role.ADMIN.name()).isEqualTo("ADMIN");
    }

    @Test
    void shouldHaveUserRole() {
        assertThat(Role.USER).isNotNull();
        assertThat(Role.USER.name()).isEqualTo("USER");
    }

    @Test
    void shouldHaveTwoRoles() {
        assertThat(Role.values()).hasSize(2);
    }

    @Test
    void shouldConvertFromString() {
        assertThat(Role.valueOf("ADMIN")).isEqualTo(Role.ADMIN);
        assertThat(Role.valueOf("USER")).isEqualTo(Role.USER);
    }
}
