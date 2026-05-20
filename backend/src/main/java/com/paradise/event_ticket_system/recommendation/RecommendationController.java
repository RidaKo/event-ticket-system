package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class RecommendationController {

    /** Seeded demo user for preference-based scoring when the client is not authenticated. */
    private static final String DEMO_USER_EMAIL = "alex@demo.local";

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationService recommendationService,
                                    UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/recommended")
    public RecommendationResponse recommended(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer limit,
            Authentication authentication
    ) {
        Set<String> categorySlugs = parseToLowerSlugs(categories);
        Set<String> tagSlugs = parseToLowerSlugs(tags);

        RecommendationFilters filters = new RecommendationFilters(
                categorySlugs,
                tagSlugs,
                startDate,
                endDate,
                location
        );

        Integer userId = resolveUserId(authentication);
        return recommendationService.recommend(userId, filters, limit);
    }

    /** Authenticated user when present; otherwise seeded demo user for local browse. */
    private Integer resolveUserId(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return userRepository.findByEmailIgnoreCase(authentication.getName())
                    .map(User::getId)
                    .orElse(null);
        }
        return userRepository.findByEmailIgnoreCase(DEMO_USER_EMAIL)
                .map(User::getId)
                .orElse(null);
    }

    private Set<String> parseToLowerSlugs(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Set.of();
        }
        return raw.stream()
                .flatMap(v -> Arrays.stream(v.split(",")))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
