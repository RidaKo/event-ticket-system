package com.paradise.event_ticket_system.viewEvent.api.DTO;

import java.math.BigDecimal;

public record OrderLineResponse(
        Integer ticketTypeId,
        String ticketTypeName,
        Integer quantity,
        BigDecimal pricePerTicket,
        BigDecimal lineTotal
) {}
