package com.paradise.event_ticket_system.checkout;

import java.math.BigDecimal;
import java.util.List;

public record OrderSummaryResponse(
        List<TicketLineResponse> items,
        BigDecimal subtotal,
        String discountCode,
        BigDecimal discountAmount,
        BigDecimal total
) {
}
