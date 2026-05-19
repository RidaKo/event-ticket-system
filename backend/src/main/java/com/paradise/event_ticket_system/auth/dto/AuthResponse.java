package com.paradise.event_ticket_system.auth.dto;

import java.time.Instant;

public record AuthResponse(
        String token,
        Instant expiresAt,
        CurrentUserResponse user
) {}
