package com.paradise.event_ticket_system.recommendation.dto;

import java.util.List;

public record RecommendationResponse(
        List<RecommendedEventDto> items,
        boolean fallbackUsed,
        /** True when results use a logged-in user's saved preferences (show "Recommended for you"). */
        boolean personalized,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
