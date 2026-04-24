package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.event.Category;
import com.paradise.event_ticket_system.event.Event;
import com.paradise.event_ticket_system.event.EventRepository;
import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
import com.paradise.event_ticket_system.recommendation.dto.RecommendedEventDto;
import com.paradise.event_ticket_system.user.UserPreferences;
import com.paradise.event_ticket_system.user.UserPreferencesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ranks upcoming published events for a user using a lightweight rule-based score.
 *
 * Signals (see plan, Ticket 1):
 *   Persisted user preferences: preferredCategories, preferredTags, homeCity.
 *   Request-time filters:       categories, tagSlugs, startDate, endDate, location.
 *
 * Scoring (Ticket 2):
 *   +3 per matching preferred category
 *   +2 per matching preferred tag
 *   +2 if event.city == prefs.homeCity (case-insensitive)
 *   +1 if startAt is within the next 14 days
 *
 * If the scored result is shorter than the requested limit, the response is padded with
 * the next upcoming events (ordered by startAt) and fallbackUsed is set to true.
 */
@Service
public class RecommendationService {

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

    public RecommendationResponse recommend(Long userId,
                                            RecommendationFilters filters,
                                            Integer limit) {
        int effectiveLimit = (limit == null || limit <= 0)
                ? scoringProperties.getDefaultLimit()
                : limit;
        LocalDateTime now = LocalDateTime.now();
        boolean hasActiveFilters = filters != null && !filters.isEmpty();

        UserPreferences prefs = userId == null
                ? null
                : preferencesRepository.findByUserId(userId).orElse(null);

        List<Event> upcoming = eventRepository.findUpcomingPublished(now);

        List<Event> filtered = upcoming.stream()
                .filter(e -> passesFilters(e, filters))
                .toList();

        List<ScoredEvent> scored = filtered.stream()
                .map(e -> new ScoredEvent(e, score(e, prefs, now)))
                .sorted(Comparator
                        .comparingInt(ScoredEvent::score).reversed()
                        .thenComparing(se -> se.event().getStartAt()))
                .toList();

        List<Event> picks = new ArrayList<>(scored.stream()
                .limit(effectiveLimit)
                .map(ScoredEvent::event)
                .toList());

        boolean fallbackUsed = prefs == null
                || prefs.getPreferredCategories().isEmpty() && prefs.getPreferredTags().isEmpty();

        if (!hasActiveFilters && picks.size() < effectiveLimit) {
            fallbackUsed = true;
            Set<Long> pickedIds = picks.stream().map(Event::getId).collect(Collectors.toSet());
            upcoming.stream()
                    .filter(e -> !pickedIds.contains(e.getId()))
                    .sorted(Comparator.comparing(Event::getStartAt))
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
        if (filters.categories() != null && !filters.categories().isEmpty()
                && !filters.categories().contains(event.getCategory())) {
            return false;
        }
        if (filters.tagSlugs() != null && !filters.tagSlugs().isEmpty()) {
            Set<String> eventSlugs = event.getTags().stream()
                    .map(Tag::getSlug)
                    .collect(Collectors.toSet());
            boolean anyMatch = filters.tagSlugs().stream().anyMatch(eventSlugs::contains);
            if (!anyMatch) {
                return false;
            }
        }
        if (filters.startDate() != null
                && event.getStartAt().toLocalDate().isBefore(filters.startDate())) {
            return false;
        }
        if (filters.endDate() != null
                && event.getStartAt().toLocalDate().isAfter(filters.endDate())) {
            return false;
        }
        if (filters.location() != null && !filters.location().isBlank()) {
            String needle = filters.location().toLowerCase(Locale.ROOT);
            String city = event.getCity() == null ? "" : event.getCity().toLowerCase(Locale.ROOT);
            String venue = event.getVenue() == null ? "" : event.getVenue().toLowerCase(Locale.ROOT);
            if (!city.contains(needle) && !venue.contains(needle)) {
                return false;
            }
        }
        return true;
    }

    private int score(Event event, UserPreferences prefs, LocalDateTime now) {
        int score = 0;
        if (prefs != null) {
            Set<Category> preferredCategories = prefs.getPreferredCategories();
            if (preferredCategories != null && preferredCategories.contains(event.getCategory())) {
                score += scoringProperties.getCategoryMatchPoints();
            }
            Set<Tag> preferredTags = prefs.getPreferredTags();
            if (preferredTags != null && !preferredTags.isEmpty()) {
                Set<Long> preferredIds = preferredTags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toSet());
                long matches = event.getTags().stream()
                        .filter(t -> preferredIds.contains(t.getId()))
                        .count();
                score += (int) matches * scoringProperties.getTagMatchPoints();
            }
            String homeCity = prefs.getHomeCity();
            if (homeCity != null && !homeCity.isBlank()
                    && event.getCity() != null
                    && homeCity.equalsIgnoreCase(event.getCity())) {
                score += scoringProperties.getHomeCityMatchPoints();
            }
        }
        LocalDate today = now.toLocalDate();
        LocalDate eventDay = event.getStartAt().toLocalDate();
        if (!eventDay.isBefore(today)
                && eventDay.isBefore(today.plusDays(scoringProperties.getSoonWindowDays() + 1L))) {
            score += scoringProperties.getSoonWindowBonusPoints();
        }
        return score;
    }

    private record ScoredEvent(Event event, int score) {
    }
}
