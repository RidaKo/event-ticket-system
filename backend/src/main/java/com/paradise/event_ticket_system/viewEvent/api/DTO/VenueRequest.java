package com.paradise.event_ticket_system.viewEvent.api.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VenueRequest(

        @NotBlank(message = "Venue name is required")
        String name,

        @NotBlank(message = "Address is required")
        String addressLine1,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Country is required")
        @Size(min = 2, max = 2, message = "Country must be a 2-letter ISO code (e.g. LT, US)")
        String country,

        String coverPhotoUrl
) {}