package com.paradise.event_ticket_system.checkout;

import java.math.BigDecimal;

public record TicketLineResponse(
        Integer ticketTypeId,
        String name,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
