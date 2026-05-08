package com.paradise.event_ticket_system.ticket;

import java.math.BigDecimal;

public record TicketTypeResponse(
        Integer id,
        String name,
        BigDecimal price,
        int availableQuantity,
        boolean salesEnabled
) {
}
