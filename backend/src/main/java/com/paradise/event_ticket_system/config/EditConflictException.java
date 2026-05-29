package com.paradise.event_ticket_system.config;

public class EditConflictException extends RuntimeException {

    private final Object current;

    public EditConflictException(String message, Object current) {
        super(message);
        this.current = current;
    }

    public Object getCurrent() {
        return current;
    }
}
