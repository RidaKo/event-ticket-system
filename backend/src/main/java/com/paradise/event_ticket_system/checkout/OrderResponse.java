package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.order.OrderStatus;

public record OrderResponse(
        String orderNumber,
        OrderStatus status,
        Long eventId,
        String guestName,
        String guestEmail,
        OrderSummaryResponse summary
) {
}
