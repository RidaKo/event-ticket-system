package com.paradise.event_ticket_system.payment;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MockPaymentService implements PaymentService {

    @Override
    public PaymentResult charge(BigDecimal amount, PaymentMethodType methodType, String cardNumber) {
        String normalized = cardNumber == null ? "" : cardNumber.replaceAll("\\D", "");
        boolean successful = !normalized.endsWith("0000");
        String last4 = normalized.length() >= 4 ? normalized.substring(normalized.length() - 4) : null;
        return new PaymentResult(successful, "mock_" + UUID.randomUUID(), last4);
    }
}
