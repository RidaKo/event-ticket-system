package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.model.PurchaseConfirmationTicketLine;
import java.time.OffsetDateTime;
import java.util.List;

public record PurchaseConfirmationEmailData(
        Long deliveryId,
        Long orderId,
        String orderReference,
        String attendeeEmail,
        String eventTitle,
        OffsetDateTime eventDateTime,
        String eventLocation,
        int totalQuantity,
        List<PurchaseConfirmationTicketLine> ticketLines,
        String orderAccessUrl,
        List<TicketEmailData> tickets
) {
    public record TicketEmailData(
            String ticketType,
            String ticketCode,
            String contentId,
            String verifyUrl
    ) {
    }
}
