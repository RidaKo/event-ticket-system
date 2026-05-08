package com.paradise.event_ticket_system.checkout;

import java.time.Instant;

public record EventSummaryResponse(
        Integer id,
        String title,
        String description,
        Instant startsAt,
        Instant endsAt,
        String venueName,
        String address,
        String city,
        String country
) {
}
