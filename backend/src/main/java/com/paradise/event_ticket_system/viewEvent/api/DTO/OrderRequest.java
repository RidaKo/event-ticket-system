package com.paradise.event_ticket_system.viewEvent.api.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequest(
        @NotNull(message = "Event ID is required")
        Integer eventId,

        Integer userId,

        String guestEmail,
        String guestName,
        String guestPhone,

        @NotEmpty(message = "At least one ticket type is required")
        List<@Valid OrderItemRequest> items
) {}
