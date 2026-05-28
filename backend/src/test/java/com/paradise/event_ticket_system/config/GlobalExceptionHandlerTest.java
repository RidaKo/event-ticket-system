package com.paradise.event_ticket_system.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Map;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void optimisticLockException_returns409WithMessage() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException(Object.class, 1);

        ResponseEntity<Map<String, String>> response = handler.handleOptimisticLock(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message")).isNotBlank();
    }
}
