package com.paradise.event_ticket_system.viewEvent.api.DTO;

import com.paradise.event_ticket_system.event.EventStatus;

import java.time.Instant;
import java.util.List;

public record EventResponse(
        Integer id,
        String title,
        String slug,
        String description,
        EventStatus status,
        Instant startDatetime,
        Instant endDatetime,
        String timezone,
        Integer minAge,
        String coverPhotoUrl,
        String photoUrls,
        String categoryName,
        String organizerName,
        String venueName,
        String address,
        String city,
        String country,
        VenueResponse venue,
        Double averageRating,
        Integer reviewCount,
        List<ReviewResponse> reviews
) {}
