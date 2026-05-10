package com.paradise.event_ticket_system.viewEvent.api.DTO;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketTypeResponse(
        Integer id,
        Integer eventId,
        String name,
        String description,
        BigDecimal price,
        String currency,
        Integer quantityTotal,
        Integer quantitySold,
        Integer quantityAvailable,
        Instant saleStart,
        Instant saleEnd,
        Integer maxPerOrder,
        Boolean isActive
) {}
