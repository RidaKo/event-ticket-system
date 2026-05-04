package com.paradise.event_ticket_system.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateEventRequest(
        @NotBlank String title,
        String description,
        @NotBlank String categorySlug,
        @NotBlank String venueName,
        @NotBlank String city,
        @NotNull LocalDate date,
        @NotNull LocalTime time,
        boolean publish
) {
}
