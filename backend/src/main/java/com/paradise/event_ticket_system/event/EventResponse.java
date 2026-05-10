package com.paradise.event_ticket_system.event;

import java.time.Instant;

public record EventResponse(
        Integer id,
        String title,
        String description,
        Instant startsAt,
        Instant endsAt,
        String venueName,
        String address,
        String city,
        String country,
        boolean salesEnabled
) {
}
