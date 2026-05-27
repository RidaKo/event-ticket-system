package com.paradise.event_ticket_system.user.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateUserPreferencesRequest(
        List<@Size(max = 64) String> categorySlugs,
        List<@Size(max = 64) String> tagSlugs,
        @Size(max = 255) String homeCity,
        Long version
) {}
