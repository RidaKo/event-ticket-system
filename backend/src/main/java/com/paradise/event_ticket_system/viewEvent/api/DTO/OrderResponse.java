package com.paradise.event_ticket_system.viewEvent.api.DTO;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Integer orderId,
        Integer eventId,
        String status,
        BigDecimal subtotal,
        BigDecimal fees,
        BigDecimal tax,
        BigDecimal total,
        String currency,
        List<OrderLineResponse> items
) {}
