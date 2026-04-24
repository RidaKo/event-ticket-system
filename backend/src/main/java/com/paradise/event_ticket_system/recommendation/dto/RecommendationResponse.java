package com.paradise.event_ticket_system.recommendation.dto;

import java.util.List;

public record RecommendationResponse(
        List<RecommendedEventDto> items,
        boolean fallbackUsed
) {
}
