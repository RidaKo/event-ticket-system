package com.paradise.event_ticket_system.payment;

public record PaymentResult(boolean successful, String providerReference, String cardLast4) {
}
