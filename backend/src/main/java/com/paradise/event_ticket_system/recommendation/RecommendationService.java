package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.event.CheckoutCatalogRules;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
import com.paradise.event_ticket_system.recommendation.dto.RecommendedEventDto;
import com.paradise.event_ticket_system.user.UserPreferences;
import com.paradise.event_ticket_system.user.UserPreferencesRepository;
import com.paradise.event_ticket_system.viewEvent.domain.EventRepository;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ranks upcoming discoverable events for a user using a lightweight rule-based score.
 *
 * Discoverable statuses align with {@link CheckoutCatalogRules} (excludes DRAFT/CANCELLED/etc.)
 * until event lifecycle is unified in a follow-up change.
 */
@Service
@AuditedBusinessAction
public class RecommendationService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_RECOMMENDATION_CANDIDATES = 500;

    private final EventRepository eventRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;
    private final RecommendationScoringProperties scoringProperties;
    private final RecommendationScoringStrategy scoringStrategy;

    public RecommendationService(EventRepository eventRepository,
                                 UserPreferencesRepository preferencesRepository,
                                 UserRepository userRepository,
                                 RecommendationScoringProperties scoringProperties,
                                 RecommendationScoringStrategy scoringStrategy) {
        this.eventRepository = eventRepository;
        this.preferencesRepository = preferencesRepository;
        this.userRepository = userRepository;
        this.scoringProperties = scoringProperties;
        this.scoringStrategy = scoringStrategy;
    }

    public RecommendationResponse recommendForEmail(String email,
                                                    RecommendationFilters filters,
                                                    Integer page,
                                                    Integer size) {
        Integer userId = email == null
                ? null
                : userRepository.findByEmailIgnoreCase(email)
                        .map(com.paradise.event_ticket_system.model.User::getId)
                        .orElse(null);
        return recommend(userId, filters, page, size);
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

        Page<Integer> candidateIdPage = eventRepository.findUpcomingDiscoverableFilteredIds(
                now,
                CheckoutCatalogRules.closedStatusNames(),
                safeLowerValues(filters == null ? null : filters.categorySlugs()),
                safeCount(filters == null ? null : filters.categorySlugs()),
                safeLowerValues(filters == null ? null : filters.tagSlugs()),
                safeCount(filters == null ? null : filters.tagSlugs()),
                startInstant(filters),
                endInstantExclusive(filters),
                locationLike(filters),
                PageRequest.of(0, MAX_RECOMMENDATION_CANDIDATES)
        );
        List<Event> filtered = loadEventsInCandidateOrder(candidateIdPage.getContent());

        List<ScoredEvent> scored = filtered.stream()
                .map(e -> new ScoredEvent(e, scoringStrategy.score(e, prefs, now)))
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
            filtered.stream()
                    .filter(e -> !pickedIds.contains(e.getId()))
                    .sorted(Comparator.comparing(Event::getStartDatetime))
                    .limit((long) effectiveSize - orderedEvents.size())
                    .forEach(orderedEvents::add);
        }

        int totalElements = (int) Math.min(candidateIdPage.getTotalElements(), orderedEvents.size());
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

    private List<Event> loadEventsInCandidateOrder(List<Integer> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Integer, Integer> orderById = new LinkedHashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            orderById.put(ids.get(i), i);
        }
        return eventRepository.findDiscoverableDetailsByIdIn(ids).stream()
                .sorted(Comparator.comparingInt(event -> orderById.getOrDefault(event.getId(), Integer.MAX_VALUE)))
                .toList();
    }

    private static boolean hasValues(Collection<?> values) {
        return values != null && !values.isEmpty();
    }

    private static long safeCount(Collection<?> values) {
        return values == null ? 0 : values.size();
    }

    private static Set<String> safeLowerValues(Set<String> values) {
        if (!hasValues(values)) {
            return Set.of("__none__");
        }
        return values.stream()
                .filter(RecommendationService::hasText)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    private static Instant startInstant(RecommendationFilters filters) {
        LocalDate startDate = filters == null ? null : filters.startDate();
        return startDate == null ? null : startDate.atStartOfDay(DEFAULT_ZONE).toInstant();
    }

    private static Instant endInstantExclusive(RecommendationFilters filters) {
        LocalDate endDate = filters == null ? null : filters.endDate();
        return endDate == null ? null : endDate.plusDays(1).atStartOfDay(DEFAULT_ZONE).toInstant();
    }

    private static String locationLike(RecommendationFilters filters) {
        String location = filters == null ? null : filters.location();
        return hasText(location) ? "%" + location.toLowerCase(Locale.ROOT).trim() + "%" : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ScoredEvent(Event event, int score) {
    }
}
