package com.paradise.event_ticket_system.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String OPTIMISTIC_LOCK_CONFLICT = "OPTIMISTIC_LOCK_CONFLICT";

    @ExceptionHandler(EditConflictException.class)
    public ResponseEntity<Map<String, Object>> handleEditConflict(EditConflictException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", OPTIMISTIC_LOCK_CONFLICT);
        body.put("message", ex.getMessage());
        body.put("current", ex.getCurrent());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "code", OPTIMISTIC_LOCK_CONFLICT,
                        "message",
                        "This record was modified by someone else. Refresh and try again."));
    }
}
