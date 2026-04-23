package com.paradise.event_ticket_system.checkout;

import java.time.LocalDateTime;

public record EventSummaryResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String venueName,
        String address,
        String city,
        String country
) {
}
