package com.paradise.event_ticket_system.payment;

import java.math.BigDecimal;

public interface PaymentService {

    PaymentResult charge(BigDecimal amount, PaymentMethodType methodType, String cardNumber);
}
