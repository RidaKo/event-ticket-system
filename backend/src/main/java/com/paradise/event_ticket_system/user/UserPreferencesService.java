package com.paradise.event_ticket_system.user;

import com.paradise.event_ticket_system.category.CategoryRepository;
import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.event.TagRepository;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.user.dto.UpdateUserPreferencesRequest;
import com.paradise.event_ticket_system.user.dto.UserPreferencesResponse;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class UserPreferencesService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public UserPreferencesService(UserRepository userRepository,
                                  UserPreferencesRepository preferencesRepository,
                                  CategoryRepository categoryRepository,
                                  TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public UserPreferencesResponse getForEmail(String email) {
        User user = requireUser(email);
        return preferencesRepository.findByUserId(user.getId())
                .map(UserPreferencesResponse::from)
                .orElse(UserPreferencesResponse.empty());
    }

    @Transactional
    public UserPreferencesResponse saveForEmail(String email, UpdateUserPreferencesRequest request) {
        User user = requireUser(email);
        UserPreferences preferences = preferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createPreferences(user));

        preferences.setPreferredCategories(resolveCategories(request.categorySlugs()));
        preferences.setPreferredTags(resolveTags(request.tagSlugs()));
        preferences.setHomeCity(normalizeHomeCity(request.homeCity()));

        return UserPreferencesResponse.from(preferencesRepository.save(preferences));
    }

    private User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private UserPreferences createPreferences(User user) {
        UserPreferences preferences = new UserPreferences();
        preferences.setUser(user);
        preferences.setPreferredCategories(new HashSet<>());
        preferences.setPreferredTags(new HashSet<>());
        return preferences;
    }

    private Set<Category> resolveCategories(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            return new HashSet<>();
        }
        Set<Category> categories = new HashSet<>();
        for (String raw : slugs) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String slug = raw.trim().toLowerCase(Locale.ROOT);
            Category category = categoryRepository.findBySlug(slug)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unknown category: " + raw));
            categories.add(category);
        }
        return categories;
    }

    private Set<Tag> resolveTags(List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>();
        for (String raw : slugs) {
            if (raw == null || raw.isBlank()) {
                continue;
            }
            String slug = raw.trim().toLowerCase(Locale.ROOT);
            Tag tag = tagRepository.findBySlug(slug)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Unknown tag: " + raw));
            tags.add(tag);
        }
        return tags;
    }

    private static String normalizeHomeCity(String homeCity) {
        if (homeCity == null) {
            return null;
        }
        String trimmed = homeCity.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
