package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.config.DemoUserProvider;
import com.paradise.event_ticket_system.event.Category;
import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RecommendationServiceIntegrationTest {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private DemoUserProvider demoUserProvider;

    @Test
    void filteredRecommendationsDoNotFallbackToUnmatchedEvents() {
        RecommendationFilters filters = new RecommendationFilters(
                Set.of(Category.SPORTS),
                Set.of(),
                null,
                null,
                "Vilnius"
        );

        RecommendationResponse response = recommendationService.recommend(
                demoUserProvider.getDemoUserId(),
                filters,
                3
        );

        assertNotNull(response);
        assertFalse(response.fallbackUsed());
        assertEquals(1, response.items().size());
        assertTrue(response.items().stream().allMatch(item -> item.category() == Category.SPORTS));
        assertTrue(response.items().stream().allMatch(item -> "Vilnius".equalsIgnoreCase(item.city())));
    }
}
