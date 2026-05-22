package com.paradise.event_ticket_system.recommendation;

import java.time.LocalDate;
import java.util.Set;

/**
 * Request-time filters for the recommendation feed.
 *
 * @param categorySlugs lower-case category slugs; an event must match every selected slug
 * @param tagSlugs      lower-case tag slugs; an event must include every selected tag
 */
public record RecommendationFilters(
        Set<String> categorySlugs,
        Set<String> tagSlugs,
        LocalDate startDate,
        LocalDate endDate,
        String location
) {
    public boolean isEmpty() {
        return (categorySlugs == null || categorySlugs.isEmpty())
                && (tagSlugs == null || tagSlugs.isEmpty())
                && startDate == null
                && endDate == null
                && (location == null || location.isBlank());
    }
}
