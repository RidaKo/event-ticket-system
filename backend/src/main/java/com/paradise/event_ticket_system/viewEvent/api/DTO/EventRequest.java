package com.paradise.event_ticket_system.viewEvent.api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record EventRequest(

        @NotNull(message = "Venue ID is required")
        Integer venueId,

        @NotNull(message = "Organizer ID is required")
        Integer organizerId,

        @NotNull(message = "Category ID is required")
        Integer categoryId,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Slug is required")
        String slug,

        String description,

        @NotBlank(message = "Status is required")
        String status,

        @NotNull(message = "Start date is required")
        Instant startDatetime,

        @NotNull(message = "End date is required")
        Instant endDatetime,

        @NotBlank(message = "Timezone is required")
        String timezone,

        Integer minAge,
        String coverPhotoUrl,
        String photoUrls
) {}