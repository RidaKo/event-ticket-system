package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.model.EmailDeliveryStatus;
import java.time.Instant;

public record ConfirmationEmailResponse(
        EmailDeliveryStatus status,
        String attendeeEmail,
        Instant sentAt,
        String failureReason
) {
}
