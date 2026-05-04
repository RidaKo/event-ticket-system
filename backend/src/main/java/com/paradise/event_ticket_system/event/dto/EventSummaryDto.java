package com.paradise.event_ticket_system.event.dto;

import com.paradise.event_ticket_system.model.Event;

public record EventSummaryDto(
        Integer id,
        String title,
        String status,
        String slug,
        String categorySlug,
        String venueName,
        String city,
        String startDatetime
) {
    public static EventSummaryDto from(Event event) {
        return new EventSummaryDto(
                event.getId(),
                event.getTitle(),
                event.getStatus(),
                event.getSlug(),
                event.getCategory() != null ? event.getCategory().getSlug() : null,
                event.getVenue() != null ? event.getVenue().getName() : null,
                event.getVenue() != null ? event.getVenue().getCity() : null,
                event.getStartDatetime() != null ? event.getStartDatetime().toString() : null
        );
    }
}
