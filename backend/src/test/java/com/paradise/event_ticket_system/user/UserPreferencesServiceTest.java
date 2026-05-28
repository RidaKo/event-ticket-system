package com.paradise.event_ticket_system.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paradise.event_ticket_system.category.CategoryRepository;
import com.paradise.event_ticket_system.event.TagRepository;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.user.dto.UpdateUserPreferencesRequest;
import com.paradise.event_ticket_system.user.dto.UserPreferencesResponse;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

class UserPreferencesServiceTest {

    private UserRepository userRepository;
    private UserPreferencesRepository preferencesRepository;
    private UserPreferencesService service;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        preferencesRepository = Mockito.mock(UserPreferencesRepository.class);
        service = new UserPreferencesService(
                userRepository,
                preferencesRepository,
                Mockito.mock(CategoryRepository.class),
                Mockito.mock(TagRepository.class)
        );
    }

    private User userWithEmail(String email) {
        User user = new User();
        user.setId(1);
        user.setEmail(email);
        return user;
    }

    private UserPreferences preferencesWithVersion(User user, long version) {
        UserPreferences prefs = new UserPreferences();
        prefs.setId(1);
        prefs.setUser(user);
        prefs.setPreferredCategories(new HashSet<>());
        prefs.setPreferredTags(new HashSet<>());
        prefs.setVersion(version);
        return prefs;
    }

    @Test
    void saveForEmail_whenVersionMatches_saves() {
        User user = userWithEmail("alice@example.com");
        UserPreferences existing = preferencesWithVersion(user, 3L);
        Mockito.when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        Mockito.when(preferencesRepository.findByUserId(1)).thenReturn(Optional.of(existing));
        Mockito.when(preferencesRepository.saveAndFlush(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        UserPreferencesResponse result = service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), "Vilnius", 3L)
        );

        assertThat(result).isNotNull();
        Mockito.verify(preferencesRepository).saveAndFlush(existing);
    }

    @Test
    void saveForEmail_whenVersionMismatches_throws409() {
        User user = userWithEmail("alice@example.com");
        UserPreferences existing = preferencesWithVersion(user, 4L);
        Mockito.when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        Mockito.when(preferencesRepository.findByUserId(1)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), null, 3L)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void saveForEmail_whenNoExistingPreferences_skipsVersionCheck() {
        User user = userWithEmail("alice@example.com");
        Mockito.when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        Mockito.when(preferencesRepository.findByUserId(1)).thenReturn(Optional.empty());
        UserPreferences saved = preferencesWithVersion(user, 0L);
        Mockito.when(preferencesRepository.saveAndFlush(Mockito.any())).thenReturn(saved);

        UserPreferencesResponse result = service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), null, 99L)
        );

        assertThat(result).isNotNull();
    }

    @Test
    void saveForEmail_whenVersionIsNullAndPreferencesExist_throws409() {
        User user = userWithEmail("alice@example.com");
        UserPreferences existing = preferencesWithVersion(user, 4L);
        Mockito.when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        Mockito.when(preferencesRepository.findByUserId(1)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), null, null)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
