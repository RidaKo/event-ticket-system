package com.paradise.event_ticket_system.admission;

import java.time.Instant;

public record TicketVerifyResponse(
    String ticketCode,
    String status,
    String ticketTypeName,
    String eventTitle,
    Instant eventStartsAt,
    boolean checkedIn
) {
}
