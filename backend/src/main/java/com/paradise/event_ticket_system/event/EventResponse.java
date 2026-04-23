package com.paradise.event_ticket_system.event;

import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        String title,
        String description,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String venueName,
        String address,
        String city,
        String country,
        boolean salesEnabled
) {
}
