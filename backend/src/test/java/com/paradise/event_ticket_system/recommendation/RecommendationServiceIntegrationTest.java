package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("seed")
class RecommendationServiceIntegrationTest {

    private static final String DEMO_USER_EMAIL = "alex@demo.local";

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserRepository userRepository;

    private Integer demoUserId;

    @BeforeEach
    void setUp() {
        demoUserId = userRepository.findByEmailIgnoreCase(DEMO_USER_EMAIL)
                .map(User::getId)
                .orElse(null);
        assertNotNull(demoUserId, "Seed user " + DEMO_USER_EMAIL + " must exist");
    }

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
                demoUserId,
                filters,
                3
        );

        assertNotNull(response);
        assertTrue(response.personalized());
        assertFalse(response.fallbackUsed());
        assertFalse(response.items().isEmpty());
        assertTrue(response.items().stream().allMatch(item -> "SPORTS".equals(item.category())));
        assertTrue(response.items().stream().allMatch(item -> "Vilnius".equalsIgnoreCase(item.city())));
    }

    @Test
    void anonymousUserIsNotPersonalized() {
        RecommendationResponse response = recommendationService.recommend(
                null,
                new RecommendationFilters(Set.of(), Set.of(), null, null, null),
                20
        );

        assertFalse(response.personalized());
        assertFalse(response.fallbackUsed());
        assertFalse(response.items().isEmpty());
    }

    @Test
    void authenticatedUserWithoutPreferencesIsNotPersonalized() {
        Integer organizerId = userRepository.findByEmailIgnoreCase("organizer@test.com")
                .map(User::getId)
                .orElse(null);
        assertNotNull(organizerId);

        RecommendationResponse response = recommendationService.recommend(
                organizerId,
                new RecommendationFilters(Set.of(), Set.of(), null, null, null),
                20
        );

        assertFalse(response.personalized());
        assertFalse(response.items().isEmpty());
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
                demoUserId,
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
                demoUserId,
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
    void filteredRecommendationsMatchAnySelectedCategory() {
        RecommendationFilters filters = new RecommendationFilters(
                Set.of("music", "food"),
                Set.of(),
                null,
                null,
                null
        );

        RecommendationResponse response = recommendationService.recommend(
                demoUserId,
                filters,
                20
        );

        assertNotNull(response);
        assertFalse(response.fallbackUsed());
        assertFalse(response.items().isEmpty());
        assertTrue(response.items().stream().allMatch(item ->
                "MUSIC".equals(item.category()) || "FOOD".equals(item.category())));
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
                demoUserId,
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
                demoUserId,
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
                demoUserId,
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
