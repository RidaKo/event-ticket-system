package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.payment.PaymentStatus;

public record PaymentResponse(
        String orderNumber,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus
) {
}
