package com.paradise.event_ticket_system.user.dto;

import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.user.UserPreferences;

import java.util.List;
import java.util.Locale;

public record UserPreferencesResponse(
        List<String> categorySlugs,
        List<String> tagSlugs,
        String homeCity
) {
    public static UserPreferencesResponse empty() {
        return new UserPreferencesResponse(List.of(), List.of(), null);
    }

    public static UserPreferencesResponse from(UserPreferences preferences) {
        if (preferences == null) {
            return empty();
        }
        List<String> categories = preferences.getPreferredCategories() == null
                ? List.of()
                : preferences.getPreferredCategories().stream()
                        .map(Category::getSlug)
                        .map(slug -> slug.toLowerCase(Locale.ROOT))
                        .sorted()
                        .toList();
        List<String> tags = preferences.getPreferredTags() == null
                ? List.of()
                : preferences.getPreferredTags().stream()
                        .map(Tag::getSlug)
                        .map(slug -> slug.toLowerCase(Locale.ROOT))
                        .sorted()
                        .toList();
        String homeCity = preferences.getHomeCity();
        if (homeCity != null && homeCity.isBlank()) {
            homeCity = null;
        }
        return new UserPreferencesResponse(categories, tags, homeCity);
    }
}
