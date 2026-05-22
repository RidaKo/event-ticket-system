package com.paradise.event_ticket_system.recommendation.dto;

import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.model.Event;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public record RecommendedEventDto(
        Integer id,
        String title,
        String description,
        Instant startAt,
        Instant endAt,
        String city,
        String venue,
        String category,
        List<String> tags,
        String imageUrl
) {
    public static RecommendedEventDto from(Event e) {
        List<String> tagLabels = e.getTags().stream()
                .map(Tag::getLabel)
                .sorted()
                .toList();
        String venueName = e.getVenue() == null ? null : e.getVenue().getName();
        String city = e.getVenue() == null ? null : e.getVenue().getCity();
        String categoryValue = e.getCategory() == null
                ? null
                : e.getCategory().getSlug().toUpperCase(Locale.ROOT);
        return new RecommendedEventDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStartDatetime(),
                e.getEndDatetime(),
                city,
                venueName,
                categoryValue,
                tagLabels,
                e.getCoverPhotoUrl()
        );
    }

    public static Comparator<RecommendedEventDto> byStart() {
        return Comparator.comparing(RecommendedEventDto::startAt);
    }
}
