package com.paradise.event_ticket_system.viewEvent.api.DTO;

import com.paradise.event_ticket_system.event.EventStatus;

public record UpdateEventStatusRequest(
        EventStatus status
) {}
