package com.paradise.event_ticket_system.recommendation.dto;

import com.paradise.event_ticket_system.event.Category;
import com.paradise.event_ticket_system.event.Event;

import java.time.LocalDateTime;
import java.util.List;

public record RecommendedEventDto(
        Long id,
        String title,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String city,
        String venue,
        Category category,
        List<String> tags,
        String imageUrl
) {
    public static RecommendedEventDto from(Event e) {
        List<String> tagLabels = e.getTags().stream()
                .map(t -> t.getLabel())
                .sorted()
                .toList();
        return new RecommendedEventDto(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStartAt(),
                e.getEndAt(),
                e.getCity(),
                e.getVenue(),
                e.getCategory(),
                tagLabels,
                e.getImageUrl()
        );
    }
}
