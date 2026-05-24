package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.payment.PaymentMethodType;
import java.time.LocalDateTime;

public record UserOrderListItemResponse(
        String orderNumber,
        OrderStatus status,
        LocalDateTime confirmedAt,
        EventSummaryResponse event,
        OrderSummaryResponse summary,
        PaymentMethodType paymentMethod,
        String cardLast4
) {
}
