package com.paradise.event_ticket_system.category;

public record CategoryRequest(
        String name,
        String slug,
        String iconUrl
) {}