package com.paradise.event_ticket_system.viewEvent.api.DTO;

import java.time.Instant;
import java.util.List;

public record EventResponse(
        Integer id,
        String title,
        String slug,
        String description,
        String status,
        Instant startDatetime,
        Instant endDatetime,
        String timezone,
        Integer minAge,
        String coverPhotoUrl,
        String photoUrls,
        String categoryName,
        String organizerName,
        VenueResponse venue,
        Double averageRating,
        Integer reviewCount,
        List<ReviewResponse> reviews
) {}