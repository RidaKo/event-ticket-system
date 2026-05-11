package com.paradise.event_ticket_system.viewEvent.api.DTO;

import java.math.BigDecimal;
import java.time.Instant;

public record ReviewResponse(
        Integer id,
        String userName,
        BigDecimal rating,
        String comment,
        Instant createdAt
) {}