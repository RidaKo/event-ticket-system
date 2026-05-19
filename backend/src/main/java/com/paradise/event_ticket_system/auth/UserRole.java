package com.paradise.event_ticket_system.auth;

public enum UserRole {
    USER,
    ORGANIZER,
    ADMIN,
    GUEST;

    public String authority() {
        return "ROLE_" + name();
    }
}
