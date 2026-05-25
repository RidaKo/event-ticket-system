package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.event.CheckoutCatalogRules;
import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
import com.paradise.event_ticket_system.recommendation.dto.RecommendedEventDto;
import com.paradise.event_ticket_system.user.UserPreferences;
import com.paradise.event_ticket_system.user.UserPreferencesRepository;
import com.paradise.event_ticket_system.viewEvent.domain.EventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ranks upcoming discoverable events for a user using a lightweight rule-based score.
 *
 * Discoverable statuses align with {@link CheckoutCatalogRules} (excludes DRAFT/CANCELLED/etc.)
 * until event lifecycle is unified in a follow-up change.
 */
@Service
public class RecommendationService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final int MAX_PAGE_SIZE = 50;

    private final EventRepository eventRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final RecommendationScoringProperties scoringProperties;

    public RecommendationService(EventRepository eventRepository,
                                 UserPreferencesRepository preferencesRepository,
                                 RecommendationScoringProperties scoringProperties) {
        this.eventRepository = eventRepository;
        this.preferencesRepository = preferencesRepository;
        this.scoringProperties = scoringProperties;
    }

    public RecommendationResponse recommend(Integer userId,
                                            RecommendationFilters filters,
                                            Integer limit) {
        return recommend(userId, filters, 0, limit);
    }

    public RecommendationResponse recommend(Integer userId,
                                            RecommendationFilters filters,
                                            Integer page,
                                            Integer size) {
        int effectivePage = page == null ? 0 : Math.max(0, page);
        int effectiveSize = size == null || size <= 0
                ? scoringProperties.getDefaultLimit()
                : Math.min(size, MAX_PAGE_SIZE);
        Instant now = Instant.now();
        boolean hasActiveFilters = filters != null && !filters.isEmpty();

        UserPreferences prefs = userId == null
                ? null
                : preferencesRepository.findByUserId(userId).orElse(null);
        boolean personalized = hasSavedPreferences(prefs);

        List<Event> upcoming = eventRepository.findUpcomingDiscoverable(now, CheckoutCatalogRules.closedStatusNames());

        List<Event> filtered = upcoming.stream()
                .filter(e -> passesFilters(e, filters))
                .toList();

        List<ScoredEvent> scored = filtered.stream()
                .map(e -> new ScoredEvent(e, score(e, prefs, now)))
                .sorted(Comparator
                        .comparingInt(ScoredEvent::score).reversed()
                        .thenComparing(se -> se.event().getStartDatetime()))
                .toList();

        List<Event> orderedEvents = new ArrayList<>(scored.stream()
                .map(ScoredEvent::event)
                .toList());

        boolean fallbackUsed = false;

        if (personalized && !hasActiveFilters && orderedEvents.size() < effectiveSize) {
            fallbackUsed = true;
            Set<Integer> pickedIds = orderedEvents.stream().map(Event::getId).collect(Collectors.toSet());
            upcoming.stream()
                    .filter(e -> !pickedIds.contains(e.getId()))
                    .sorted(Comparator.comparing(Event::getStartDatetime))
                    .limit((long) effectiveSize - orderedEvents.size())
                    .forEach(orderedEvents::add);
        }

        int totalElements = orderedEvents.size();
        long offset = (long) effectivePage * effectiveSize;
        int fromIndex = offset >= totalElements ? totalElements : (int) offset;
        int toIndex = Math.min(fromIndex + effectiveSize, totalElements);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / effectiveSize);

        List<RecommendedEventDto> items = orderedEvents.subList(fromIndex, toIndex).stream()
                .map(RecommendedEventDto::from)
                .toList();

        return new RecommendationResponse(
                items,
                fallbackUsed,
                personalized,
                effectivePage,
                effectiveSize,
                totalElements,
                totalPages,
                effectivePage + 1 < totalPages,
                effectivePage > 0
        );
    }

    private static boolean hasSavedPreferences(UserPreferences prefs) {
        if (prefs == null) {
            return false;
        }
        boolean hasCategories = prefs.getPreferredCategories() != null
                && !prefs.getPreferredCategories().isEmpty();
        boolean hasTags = prefs.getPreferredTags() != null
                && !prefs.getPreferredTags().isEmpty();
        boolean hasCity = prefs.getHomeCity() != null && !prefs.getHomeCity().isBlank();
        return hasCategories || hasTags || hasCity;
    }

    private boolean passesFilters(Event event, RecommendationFilters filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        return matchesCategories(event, filters.categorySlugs())
                && matchesTags(event, filters.tagSlugs())
                && matchesDateRange(event, filters.startDate(), filters.endDate())
                && matchesLocation(event, filters.location());
    }

    private boolean matchesCategories(Event event, Set<String> categorySlugs) {
        if (!hasValues(categorySlugs)) {
            return true;
        }
        String slug = event.getCategory() == null
                ? null
                : event.getCategory().getSlug().toLowerCase(Locale.ROOT);
        return slug != null && categorySlugs.stream().anyMatch(slug::equals);
    }

    private boolean matchesTags(Event event, Set<String> tagSlugs) {
        if (!hasValues(tagSlugs)) {
            return true;
        }
        Set<String> eventSlugs = event.getTags().stream()
                .map(Tag::getSlug)
                .map(slug -> slug.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        return tagSlugs.stream().allMatch(eventSlugs::contains);
    }

    private boolean matchesDateRange(Event event, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        LocalDate eventDay = event.getStartDatetime().atZone(DEFAULT_ZONE).toLocalDate();
        if (startDate != null && eventDay.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && eventDay.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    private boolean matchesLocation(Event event, String location) {
        if (!hasText(location)) {
            return true;
        }
        String needle = location.toLowerCase(Locale.ROOT);
        String city = event.getVenue() == null || event.getVenue().getCity() == null
                ? ""
                : event.getVenue().getCity().toLowerCase(Locale.ROOT);
        String venueName = event.getVenue() == null || event.getVenue().getName() == null
                ? ""
                : event.getVenue().getName().toLowerCase(Locale.ROOT);
        return city.contains(needle) || venueName.contains(needle);
    }

    private static boolean hasValues(Collection<?> values) {
        return values != null && !values.isEmpty();
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private int score(Event event, UserPreferences prefs, Instant now) {
        int score = 0;
        if (prefs != null) {
            Set<Category> preferredCategories = prefs.getPreferredCategories();
            if (preferredCategories != null
                    && event.getCategory() != null
                    && preferredCategories.stream()
                            .anyMatch(c -> c.getId() != null
                                    && c.getId().equals(event.getCategory().getId()))) {
                score += scoringProperties.getCategoryMatchPoints();
            }
            Set<Tag> preferredTags = prefs.getPreferredTags();
            if (preferredTags != null && !preferredTags.isEmpty()) {
                Set<Integer> preferredIds = preferredTags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toSet());
                long matches = event.getTags().stream()
                        .filter(t -> preferredIds.contains(t.getId()))
                        .count();
                score += (int) matches * scoringProperties.getTagMatchPoints();
            }
            String homeCity = prefs.getHomeCity();
            String city = event.getVenue() == null ? null : event.getVenue().getCity();
            if (homeCity != null && !homeCity.isBlank()
                    && city != null
                    && homeCity.equalsIgnoreCase(city)) {
                score += scoringProperties.getHomeCityMatchPoints();
            }
        }
        LocalDate today = now.atZone(DEFAULT_ZONE).toLocalDate();
        LocalDate eventDay = event.getStartDatetime().atZone(DEFAULT_ZONE).toLocalDate();
        if (!eventDay.isBefore(today)
                && eventDay.isBefore(today.plusDays(scoringProperties.getSoonWindowDays() + 1L))) {
            score += scoringProperties.getSoonWindowBonusPoints();
        }
        return score;
    }

    private record ScoredEvent(Event event, int score) {
    }
}
