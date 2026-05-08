package com.paradise.event_ticket_system.viewEvent.api.DTO;

import com.paradise.event_ticket_system.model.User;
import java.math.BigDecimal;
import java.time.Instant;

public record ReviewResponse(
        Integer id,
        String userName,
        BigDecimal rating,
        String comment,
        Instant createdAt
) {}