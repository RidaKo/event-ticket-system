package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.event.Category;

import java.time.LocalDate;
import java.util.Set;

public record RecommendationFilters(
        Set<Category> categories,
        Set<String> tagSlugs,
        LocalDate startDate,
        LocalDate endDate,
        String location
) {
    public boolean isEmpty() {
        return (categories == null || categories.isEmpty())
                && (tagSlugs == null || tagSlugs.isEmpty())
                && startDate == null
                && endDate == null
                && (location == null || location.isBlank());
    }
}
