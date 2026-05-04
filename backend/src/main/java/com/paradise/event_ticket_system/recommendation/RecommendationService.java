package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.event.EventRepository;
import com.paradise.event_ticket_system.event.EventStatus;
import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
import com.paradise.event_ticket_system.recommendation.dto.RecommendedEventDto;
import com.paradise.event_ticket_system.user.UserPreferences;
import com.paradise.event_ticket_system.user.UserPreferencesRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ranks upcoming published events for a user using a lightweight rule-based score.
 *
 * Signals:
 *   Persisted user preferences: preferredCategories, preferredTags, homeCity.
 *   Request-time filters:       categorySlugs, tagSlugs, startDate, endDate, location.
 *
 * Scoring (configurable via app.recommendation.scoring.*):
 *   +3 per matching preferred category
 *   +2 per matching preferred tag
 *   +2 if event venue city == prefs.homeCity (case-insensitive)
 *   +1 if startAt is within the next 14 days
 *
 * If the scored result is shorter than the requested limit AND no filters are
 * active, the response is padded with the next upcoming events (ordered by
 * startAt) and {@code fallbackUsed} is set to true.
 */
@Service
public class RecommendationService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final String PUBLISHED = EventStatus.PUBLISHED.name();

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
        int effectiveLimit = (limit == null || limit <= 0)
                ? scoringProperties.getDefaultLimit()
                : limit;
        Instant now = Instant.now();
        boolean hasActiveFilters = filters != null && !filters.isEmpty();

        UserPreferences prefs = userId == null
                ? null
                : preferencesRepository.findByUserId(userId).orElse(null);

        List<Event> upcoming = eventRepository.findUpcomingPublished(now, PUBLISHED);

        List<Event> filtered = upcoming.stream()
                .filter(e -> passesFilters(e, filters))
                .toList();

        List<ScoredEvent> scored = filtered.stream()
                .map(e -> new ScoredEvent(e, score(e, prefs, now)))
                .sorted(Comparator
                        .comparingInt(ScoredEvent::score).reversed()
                        .thenComparing(se -> se.event().getStartDatetime()))
                .toList();

        List<Event> picks = new ArrayList<>(scored.stream()
                .limit(effectiveLimit)
                .map(ScoredEvent::event)
                .toList());

        boolean fallbackUsed = prefs == null
                || (prefs.getPreferredCategories().isEmpty() && prefs.getPreferredTags().isEmpty());

        if (!hasActiveFilters && picks.size() < effectiveLimit) {
            fallbackUsed = true;
            Set<Integer> pickedIds = picks.stream().map(Event::getId).collect(Collectors.toSet());
            upcoming.stream()
                    .filter(e -> !pickedIds.contains(e.getId()))
                    .sorted(Comparator.comparing(Event::getStartDatetime))
                    .limit((long) effectiveLimit - picks.size())
                    .forEach(picks::add);
        }

        List<RecommendedEventDto> items = picks.stream()
                .map(RecommendedEventDto::from)
                .toList();

        return new RecommendationResponse(items, fallbackUsed);
    }

    private boolean passesFilters(Event event, RecommendationFilters filters) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        if (filters.categorySlugs() != null && !filters.categorySlugs().isEmpty()) {
            String slug = event.getCategory() == null
                    ? null
                    : event.getCategory().getSlug().toLowerCase(Locale.ROOT);
            if (slug == null || !filters.categorySlugs().contains(slug)) {
                return false;
            }
        }
        if (filters.tagSlugs() != null && !filters.tagSlugs().isEmpty()) {
            Set<String> eventSlugs = event.getTags().stream()
                    .map(Tag::getSlug)
                    .collect(Collectors.toSet());
            if (filters.tagSlugs().stream().noneMatch(eventSlugs::contains)) {
                return false;
            }
        }
        LocalDate eventDay = event.getStartDatetime().atZone(DEFAULT_ZONE).toLocalDate();
        if (filters.startDate() != null && eventDay.isBefore(filters.startDate())) {
            return false;
        }
        if (filters.endDate() != null && eventDay.isAfter(filters.endDate())) {
            return false;
        }
        if (filters.location() != null && !filters.location().isBlank()) {
            String needle = filters.location().toLowerCase(Locale.ROOT);
            String city = event.getVenue() == null || event.getVenue().getCity() == null
                    ? ""
                    : event.getVenue().getCity().toLowerCase(Locale.ROOT);
            String venueName = event.getVenue() == null || event.getVenue().getName() == null
                    ? ""
                    : event.getVenue().getName().toLowerCase(Locale.ROOT);
            if (!city.contains(needle) && !venueName.contains(needle)) {
                return false;
            }
        }
        return true;
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
