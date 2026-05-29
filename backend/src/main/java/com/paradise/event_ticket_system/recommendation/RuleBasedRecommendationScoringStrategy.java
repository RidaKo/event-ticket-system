package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.user.UserPreferences;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedRecommendationScoringStrategy implements RecommendationScoringStrategy {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    private final RecommendationScoringProperties scoringProperties;

    public RuleBasedRecommendationScoringStrategy(RecommendationScoringProperties scoringProperties) {
        this.scoringProperties = scoringProperties;
    }

    @Override
    public int score(Event event, UserPreferences preferences, Instant now) {
        int score = 0;
        if (preferences != null) {
            score += preferenceScore(event, preferences);
        }
        LocalDate today = now.atZone(DEFAULT_ZONE).toLocalDate();
        LocalDate eventDay = event.getStartDatetime().atZone(DEFAULT_ZONE).toLocalDate();
        if (!eventDay.isBefore(today)
                && eventDay.isBefore(today.plusDays(scoringProperties.getSoonWindowDays() + 1L))) {
            score += scoringProperties.getSoonWindowBonusPoints();
        }
        return score;
    }

    private int preferenceScore(Event event, UserPreferences preferences) {
        int score = 0;
        Set<Category> preferredCategories = preferences.getPreferredCategories();
        if (preferredCategories != null
                && event.getCategory() != null
                && preferredCategories.stream()
                .anyMatch(c -> c.getId() != null && c.getId().equals(event.getCategory().getId()))) {
            score += scoringProperties.getCategoryMatchPoints();
        }

        Set<Tag> preferredTags = preferences.getPreferredTags();
        if (preferredTags != null && !preferredTags.isEmpty()) {
            Set<Integer> preferredIds = preferredTags.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());
            long matches = event.getTags().stream()
                    .filter(t -> preferredIds.contains(t.getId()))
                    .count();
            score += (int) matches * scoringProperties.getTagMatchPoints();
        }

        String homeCity = preferences.getHomeCity();
        String city = event.getVenue() == null ? null : event.getVenue().getCity();
        if (homeCity != null && !homeCity.isBlank()
                && city != null
                && homeCity.equalsIgnoreCase(city)) {
            score += scoringProperties.getHomeCityMatchPoints();
        }
        return score;
    }
}
