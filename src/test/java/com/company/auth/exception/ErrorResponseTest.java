package com.company.auth.exception;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ErrorResponseTest {

    @Test
    void shouldCreateErrorResponseWithBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> errors = new HashMap<>();
        errors.put("field", "error");

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(now)
                .status(400)
                .error("Bad Request")
                .message("Test error")
                .validationErrors(errors)
                .build();

        assertThat(response.getTimestamp()).isEqualTo(now);
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getError()).isEqualTo("Bad Request");
        assertThat(response.getMessage()).isEqualTo("Test error");
        assertThat(response.getValidationErrors()).containsEntry("field", "error");
    }

    @Test
    void shouldCreateErrorResponseWithoutValidationErrors() {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(500)
                .error("Internal Server Error")
                .message("Server error")
                .build();

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getError()).isEqualTo("Internal Server Error");
    }
}
