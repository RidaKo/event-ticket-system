package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.payment.PaymentMethodType;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull PaymentMethodType methodType,
        String cardNumber
) {
}
