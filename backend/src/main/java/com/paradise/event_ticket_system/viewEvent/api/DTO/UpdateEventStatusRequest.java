package com.paradise.event_ticket_system.viewEvent.api.DTO;

import com.paradise.event_ticket_system.event.EventStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateEventStatusRequest(
        @NotNull EventStatus status,
        Long version,
        Boolean force
) {}
