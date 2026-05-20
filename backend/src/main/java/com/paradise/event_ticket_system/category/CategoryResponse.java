package com.paradise.event_ticket_system.category;

public record CategoryResponse(
        Integer id,
        String name,
        String slug,
        String iconUrl
) {}