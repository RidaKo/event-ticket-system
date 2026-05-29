package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.payment.PaymentMethodType;
import java.math.BigDecimal;

public record PaymentRequestedEvent(
        String orderNumber,
        BigDecimal amount,
        PaymentMethodType methodType,
        String cardNumber
) {
}
