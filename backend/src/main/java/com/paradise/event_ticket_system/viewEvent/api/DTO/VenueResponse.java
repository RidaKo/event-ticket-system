package com.paradise.event_ticket_system.viewEvent.api.DTO;

import java.math.BigDecimal;

public record VenueResponse(
        Integer id,
        String organizerName,
        String name,
        String addressLine1,
        String city,
        String country,
        BigDecimal rating,
        String coverPhotoUrl
) {}