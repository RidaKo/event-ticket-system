package com.paradise.event_ticket_system.auth.dto;

import com.paradise.event_ticket_system.auth.UserRole;

public record CurrentUserResponse(
        Integer id,
        String email,
        String fullName,
        UserRole role
) {}
