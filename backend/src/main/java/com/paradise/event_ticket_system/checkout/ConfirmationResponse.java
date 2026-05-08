package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.payment.PaymentMethodType;
import java.time.LocalDateTime;

public record ConfirmationResponse(
        String orderNumber,
        OrderStatus status,
        String guestName,
        String guestEmail,
        LocalDateTime confirmedAt,
        EventSummaryResponse event,
        OrderSummaryResponse summary,
        PaymentMethodType paymentMethod,
        String cardLast4
) {
}
