package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.config.DemoUserProvider;
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
                Set.of("sports"),
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
        assertTrue(response.items().stream().allMatch(item -> "SPORTS".equals(item.category())));
        assertTrue(response.items().stream().allMatch(item -> "Vilnius".equalsIgnoreCase(item.city())));
    }

    @Test
    void filteredRecommendationsByOutdoorTag() {
        RecommendationFilters filters = new RecommendationFilters(
                Set.of(),
                Set.of("outdoor"),
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(
                demoUserProvider.getDemoUserId(),
                filters,
                20
        );

        assertNotNull(response);
        assertFalse(response.fallbackUsed());
        assertFalse(response.items().isEmpty());
        assertTrue(response.items().stream().allMatch(item -> item.tags().contains("Outdoor")));
        assertTrue(response.items().stream().noneMatch(item -> item.tags().isEmpty()));
    }

    @Test
    void filteredRecommendationsRequireAllSelectedTags() {
        RecommendationFilters filters = new RecommendationFilters(
                Set.of(),
                Set.of("outdoor", "family"),
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(
                demoUserProvider.getDemoUserId(),
                filters,
                20
        );

        assertNotNull(response);
        assertFalse(response.fallbackUsed());
        assertFalse(response.items().isEmpty());
        assertTrue(response.items().stream().allMatch(item ->
                item.tags().contains("Outdoor") && item.tags().contains("Family")));
    }

    @Test
    void filteredRecommendationsEmptyWhenMultipleCategoriesSelected() {
        RecommendationFilters filters = new RecommendationFilters(
                Set.of("arts", "food"),
                Set.of(),
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(
                demoUserProvider.getDemoUserId(),
                filters,
                20
        );

        assertNotNull(response);
        assertFalse(response.fallbackUsed());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void filteredRecommendationsEmptyWhenNoEventMatchesAllFilters() {
        RecommendationFilters filters = new RecommendationFilters(
                Set.of("food", "music", "sports"),
                Set.of("educational", "outdoor", "family"),
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(
                demoUserProvider.getDemoUserId(),
                filters,
                20
        );

        assertNotNull(response);
        assertFalse(response.fallbackUsed());
        assertTrue(response.items().isEmpty());
    }

    @Test
    void unfilteredRecommendationsCanUseFallbackWhenResultsAreSparse() {
        RecommendationResponse response = recommendationService.recommend(
                demoUserProvider.getDemoUserId(),
                new RecommendationFilters(Set.of(), Set.of(), null, null, null),
                100
        );

        assertNotNull(response);
        assertFalse(response.items().isEmpty());
    }

    @Test
    void filteredRecommendationsMatchCategoryAndTagTogether() {
        RecommendationFilters filters = new RecommendationFilters(
                Set.of("arts"),
                Set.of("educational"),
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(
                demoUserProvider.getDemoUserId(),
                filters,
                20
        );

        assertNotNull(response);
        assertFalse(response.fallbackUsed());
        assertFalse(response.items().isEmpty());
        assertTrue(response.items().stream().allMatch(item -> "ARTS".equals(item.category())));
        assertTrue(response.items().stream().allMatch(item -> item.tags().contains("Educational")));
    }
}
