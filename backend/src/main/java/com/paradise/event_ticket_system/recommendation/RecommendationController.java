package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.recommendation.dto.RecommendationResponse;
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

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommended")
    public RecommendationResponse recommended(
            @RequestParam(required = false) List<String> categories,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
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

        String email = authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())
                ? null
                : authentication.getName();
        return recommendationService.recommendForEmail(
                email,
                filters,
                page,
                size == null ? limit : size
        );
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
