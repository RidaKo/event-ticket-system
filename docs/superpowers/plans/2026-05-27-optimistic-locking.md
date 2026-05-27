# Optimistic Locking Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add version-based optimistic locking to `Event` and `UserPreferences`, return `409 Conflict` on stale writes, and show a Mantine conflict dialog on the frontend with "Refresh data" / "Force overwrite" options.

**Architecture:** Each editable entity gains a `row_version` column managed by JPA `@Version`; every update request carries the client's last-seen version; the service compares versions before saving (JPA acts as a DB-level safety net for true races); a global exception handler catches any slipped `ObjectOptimisticLockingFailureException`; the frontend stores `version` in state, catches `err.status === 409`, and shows a shared `ConflictDialog` Mantine Modal.

**Tech Stack:** Java 21, Spring Boot 4.0.5, Spring Data JPA / Hibernate, JUnit 5, AssertJ, Mockito (plain, no Spring context); React, Mantine v7.

---

### Task 1: Database migrations

**Files:**
- Create: `backend/src/main/resources/db/migration/V11__add_event_version.sql`
- Create: `backend/src/main/resources/db/migration/V12__add_user_preferences_version.sql`

- [ ] **Create V11**

```sql
-- backend/src/main/resources/db/migration/V11__add_event_version.sql
ALTER TABLE events
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
```

- [ ] **Create V12**

```sql
-- backend/src/main/resources/db/migration/V12__add_user_preferences_version.sql
ALTER TABLE user_preferences
    ADD COLUMN row_version BIGINT NOT NULL DEFAULT 0;
```

- [ ] **Commit**

```bash
git add backend/src/main/resources/db/migration/V11__add_event_version.sql \
        backend/src/main/resources/db/migration/V12__add_user_preferences_version.sql
git commit -m "feat: add row_version columns to events and user_preferences"
```

---

### Task 2: Add `@Version` to entities

**Files:**
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/model/Event.java`
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/user/UserPreferences.java`

- [ ] **Add `@Version` to `Event`** — insert after the `@Id` field, following the same pattern as `TicketType.java:69-71`:

```java
// model/Event.java — add import at top:
import jakarta.persistence.Version;

// add field after the @Id block (after line 35):
@Version
@Column(name = "row_version", nullable = false)
private Long version;
```

- [ ] **Add `@Version` to `UserPreferences`** — insert after the `@Id` field:

```java
// user/UserPreferences.java — add import at top:
import jakarta.persistence.Version;

// add field after the @Id block (after line 34):
@Version
@Column(name = "row_version", nullable = false)
private Long version;
```

- [ ] **Verify the app starts** (Flyway + Hibernate validate must pass):

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=seed' 2>&1 | head -30
# Expected: "Started EventTicketSystemApplication"
# If "Validate failed": check column name matches migration exactly
```

- [ ] **Commit**

```bash
git add backend/src/main/java/com/paradise/event_ticket_system/model/Event.java \
        backend/src/main/java/com/paradise/event_ticket_system/user/UserPreferences.java
git commit -m "feat: add @Version to Event and UserPreferences entities"
```

---

### Task 3: Update response DTOs and EventMapper

**Files:**
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/DTO/EventResponse.java`
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/EventMapper.java`
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/user/dto/UserPreferencesResponse.java`

- [ ] **Update `EventResponse`** — add `Long version` as the second field (after `id`):

```java
package com.paradise.event_ticket_system.viewEvent.api.DTO;

import com.paradise.event_ticket_system.event.EventStatus;
import java.time.Instant;
import java.util.List;

public record EventResponse(
        Integer id,
        Long version,
        String title,
        String slug,
        String description,
        EventStatus status,
        Instant startDatetime,
        Instant endDatetime,
        String timezone,
        Integer minAge,
        String coverPhotoUrl,
        String photoUrls,
        String categoryName,
        String organizerName,
        String venueName,
        String address,
        String city,
        String country,
        VenueResponse venue,
        Double averageRating,
        Integer reviewCount,
        List<ReviewResponse> reviews
) {}
```

- [ ] **Update `EventMapper.toEventResponse()`** — pass `event.getVersion()` as the second argument:

```java
return new EventResponse(
        event.getId(),
        event.getVersion(),          // ← add this line
        event.getTitle(),
        event.getSlug(),
        event.getDescription(),
        event.getStatus(),
        event.getStartDatetime(),
        event.getEndDatetime(),
        event.getTimezone(),
        event.getMinAge(),
        event.getCoverPhotoUrl(),
        event.getPhotoUrls(),
        event.getCategory().getName(),
        event.getOrganizer().getBusinessName(),
        event.getVenue().getName(),
        event.getVenue().getAddressLine1(),
        event.getVenue().getCity(),
        event.getVenue().getCountry(),
        toVenueResponse(event.getVenue()),
        Math.round(avgRating * 10.0) / 10.0,
        reviews.size(),
        reviews.stream().map(this::toReviewResponse).toList()
);
```

- [ ] **Update `UserPreferencesResponse`** — add `Long version` field and update both factory methods:

```java
package com.paradise.event_ticket_system.user.dto;

import com.paradise.event_ticket_system.event.Tag;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.user.UserPreferences;

import java.util.List;
import java.util.Locale;

public record UserPreferencesResponse(
        List<String> categorySlugs,
        List<String> tagSlugs,
        String homeCity,
        Long version
) {
    public static UserPreferencesResponse empty() {
        return new UserPreferencesResponse(List.of(), List.of(), null, null);
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
        return new UserPreferencesResponse(categories, tags, homeCity, preferences.getVersion());
    }
}
```

- [ ] **Compile check**

```bash
cd backend && ./gradlew compileJava 2>&1 | tail -20
# Expected: BUILD SUCCESSFUL
```

- [ ] **Commit**

```bash
git add backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/DTO/EventResponse.java \
        backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/EventMapper.java \
        backend/src/main/java/com/paradise/event_ticket_system/user/dto/UserPreferencesResponse.java
git commit -m "feat: expose version field in EventResponse and UserPreferencesResponse"
```

---

### Task 4: Update request DTOs

**Files:**
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/DTO/UpdateEventStatusRequest.java`
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/user/dto/UpdateUserPreferencesRequest.java`

- [ ] **Update `UpdateEventStatusRequest`** — add nullable `version`:

```java
package com.paradise.event_ticket_system.viewEvent.api.DTO;

import com.paradise.event_ticket_system.event.EventStatus;

public record UpdateEventStatusRequest(
        EventStatus status,
        Long version
) {}
```

- [ ] **Update `UpdateUserPreferencesRequest`** — add nullable `version`:

```java
package com.paradise.event_ticket_system.user.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateUserPreferencesRequest(
        List<@Size(max = 64) String> categorySlugs,
        List<@Size(max = 64) String> tagSlugs,
        @Size(max = 255) String homeCity,
        Long version
) {}
```

- [ ] **Compile check**

```bash
cd backend && ./gradlew compileJava 2>&1 | tail -5
# Expected: BUILD SUCCESSFUL
```

- [ ] **Commit**

```bash
git add backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/DTO/UpdateEventStatusRequest.java \
        backend/src/main/java/com/paradise/event_ticket_system/user/dto/UpdateUserPreferencesRequest.java
git commit -m "feat: add version field to update request DTOs"
```

---

### Task 5: GlobalExceptionHandler

**Files:**
- Create: `backend/src/main/java/com/paradise/event_ticket_system/config/GlobalExceptionHandler.java`

- [ ] **Write the failing test** — create `backend/src/test/java/com/paradise/event_ticket_system/config/GlobalExceptionHandlerTest.java`:

```java
package com.paradise.event_ticket_system.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Map;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void optimisticLockException_returns409WithMessage() {
        ObjectOptimisticLockingFailureException ex =
                new ObjectOptimisticLockingFailureException(Object.class, 1);

        ResponseEntity<Map<String, String>> response = handler.handleOptimisticLock(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsKey("message");
        assertThat(response.getBody().get("message")).isNotBlank();
    }
}
```

- [ ] **Run test to verify it fails**

```bash
cd backend && ./gradlew test --tests "*.GlobalExceptionHandlerTest" 2>&1 | tail -15
# Expected: compilation error — GlobalExceptionHandler does not exist yet
```

- [ ] **Implement `GlobalExceptionHandler`**

```java
package com.paradise.event_ticket_system.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message",
                        "This record was modified by someone else. Refresh and try again."));
    }
}
```

- [ ] **Run test to verify it passes**

```bash
cd backend && ./gradlew test --tests "*.GlobalExceptionHandlerTest" 2>&1 | tail -10
# Expected: BUILD SUCCESSFUL, 1 test passed
```

- [ ] **Commit**

```bash
git add backend/src/main/java/com/paradise/event_ticket_system/config/GlobalExceptionHandler.java \
        backend/src/test/java/com/paradise/event_ticket_system/config/GlobalExceptionHandlerTest.java
git commit -m "feat: add GlobalExceptionHandler for optimistic lock conflicts"
```

---

### Task 6: EventService version check + controller fix

**Files:**
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/viewEvent/service/EventService.java`
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/organizer/OrganizerController.java`
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/EventController.java`
- Create: `backend/src/test/java/com/paradise/event_ticket_system/viewEvent/service/EventServiceTest.java`

- [ ] **Write the failing tests**

```java
package com.paradise.event_ticket_system.viewEvent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paradise.event_ticket_system.category.CategoryRepository;
import com.paradise.event_ticket_system.event.EventStatus;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.viewEvent.api.EventMapper;
import com.paradise.event_ticket_system.viewEvent.domain.EventRepository;
import com.paradise.event_ticket_system.viewEvent.domain.OrganizerRepository;
import com.paradise.event_ticket_system.viewEvent.domain.ReviewRepository;
import com.paradise.event_ticket_system.viewEvent.domain.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

class EventServiceTest {

    private EventRepository eventRepository;
    private EventService service;

    @BeforeEach
    void setUp() {
        eventRepository = Mockito.mock(EventRepository.class);
        service = new EventService(
                eventRepository,
                Mockito.mock(ReviewRepository.class),
                Mockito.mock(VenueRepository.class),
                Mockito.mock(OrganizerRepository.class),
                Mockito.mock(CategoryRepository.class),
                Mockito.mock(EventMapper.class)
        );
    }

    private Event eventWithVersion(int organizerId, long version) {
        Organizer organizer = new Organizer();
        organizer.setId(organizerId);
        Event event = new Event();
        event.setId(1);
        event.setOrganizer(organizer);
        event.setVersion(version);
        return event;
    }

    @Test
    void updateStatus_whenVersionMatches_appliesChange() {
        Event event = eventWithVersion(1, 5L);
        Mockito.when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        service.updateStatus(1, 1, EventStatus.PUBLISHED, 5L);

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    void updateStatus_whenVersionMismatches_throws409() {
        Event event = eventWithVersion(1, 6L); // DB is at version 6
        Mockito.when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.updateStatus(1, 1, EventStatus.PUBLISHED, 5L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void updateStatus_whenVersionIsNull_skipsCheck() {
        Event event = eventWithVersion(1, 6L);
        Mockito.when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        // should not throw
        service.updateStatus(1, 1, EventStatus.CANCELED, null);

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELED);
    }
}
```

- [ ] **Run tests to verify they fail**

```bash
cd backend && ./gradlew test --tests "*.EventServiceTest" 2>&1 | tail -20
# Expected: compilation error — updateStatus(int,int,EventStatus,Long) not defined
```

- [ ] **Update `EventService.updateStatus()`** — add `Long version` parameter and version check:

```java
@Transactional
public void updateStatus(
        Integer organizerId,
        Integer eventId,
        EventStatus status,
        Long version
) {
    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Event not found"));

    if (!event.getOrganizer().getId().equals(organizerId)) {
        throw new IllegalArgumentException("Organizer does not own this event");
    }

    if (version != null && !version.equals(event.getVersion())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This event was modified by someone else. Refresh and try again.");
    }

    event.setStatus(status);
}
```

Add to imports in `EventService.java`:
```java
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
```

- [ ] **Add PATCH endpoint to `OrganizerController`** — full file after changes:

```java
package com.paradise.event_ticket_system.organizer;

import com.paradise.event_ticket_system.viewEvent.api.DTO.EventResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.UpdateEventStatusRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueResponse;
import com.paradise.event_ticket_system.viewEvent.service.EventService;
import com.paradise.event_ticket_system.viewEvent.service.VenueService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizers")
public class OrganizerController {

    private final EventService eventService;
    private final VenueService venueService;

    public OrganizerController(EventService eventService, VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    @GetMapping("/{organizerId}/events")
    @ApiResponse(responseCode = "200", description = "Organizer events retrieved successfully")
    public ResponseEntity<List<EventResponse>> getEventsByOrganizerId(
            @PathVariable Integer organizerId) {
        return ResponseEntity.ok(eventService.getEventsByOrganizerId(organizerId));
    }

    @GetMapping("/{organizerId}/venues")
    public ResponseEntity<List<VenueResponse>> getVenuesByOrganizerId(
            @PathVariable Integer organizerId) {
        return ResponseEntity.ok(venueService.getVenuesByOrganizerId(organizerId));
    }

    @PatchMapping("/{organizerId}/events/{eventId}/status")
    public ResponseEntity<Void> updateEventStatus(
            @PathVariable Integer organizerId,
            @PathVariable Integer eventId,
            @RequestBody UpdateEventStatusRequest request
    ) {
        eventService.updateStatus(organizerId, eventId, request.status(), request.version());
        return ResponseEntity.ok().build();
    }
}
```

- [ ] **Remove stale PATCH from `EventController`** — delete the `updateStatus` method entirely. The remaining `EventController` after removal:

```java
package com.paradise.event_ticket_system.viewEvent.api;

import com.paradise.event_ticket_system.viewEvent.api.DTO.EventRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.EventResponse;
import com.paradise.event_ticket_system.viewEvent.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Browse and view event details")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Get all events", description = "Returns a list of all events with venue, ratings and category")
    @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Returns full event details including venue, reviews and ratings")
    @ApiResponse(responseCode = "200", description = "Event found")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Integer id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @Operation(summary = "Create an event")
    @ApiResponse(responseCode = "201", description = "Event created successfully")
    @ApiResponse(responseCode = "404", description = "Venue, organizer or category not found")
    public ResponseEntity<EventResponse> createEvent(@RequestBody @Valid EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }
}
```

- [ ] **Run the EventService tests**

```bash
cd backend && ./gradlew test --tests "*.EventServiceTest" 2>&1 | tail -10
# Expected: BUILD SUCCESSFUL, 3 tests passed
```

- [ ] **Run full test suite to catch regressions**

```bash
cd backend && ./gradlew test 2>&1 | tail -15
# Expected: BUILD SUCCESSFUL
```

- [ ] **Commit**

```bash
git add backend/src/main/java/com/paradise/event_ticket_system/viewEvent/service/EventService.java \
        backend/src/main/java/com/paradise/event_ticket_system/organizer/OrganizerController.java \
        backend/src/main/java/com/paradise/event_ticket_system/viewEvent/api/EventController.java \
        backend/src/test/java/com/paradise/event_ticket_system/viewEvent/service/EventServiceTest.java
git commit -m "feat: event status update with version check; fix endpoint path"
```

---

### Task 7: UserPreferencesService version check

**Files:**
- Modify: `backend/src/main/java/com/paradise/event_ticket_system/user/UserPreferencesService.java`
- Create: `backend/src/test/java/com/paradise/event_ticket_system/user/UserPreferencesServiceTest.java`

- [ ] **Write the failing tests**

```java
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
        Mockito.when(preferencesRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        UserPreferencesResponse result = service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), "Vilnius", 3L)
        );

        assertThat(result).isNotNull();
        Mockito.verify(preferencesRepository).save(existing);
    }

    @Test
    void saveForEmail_whenVersionMismatches_throws409() {
        User user = userWithEmail("alice@example.com");
        UserPreferences existing = preferencesWithVersion(user, 4L); // DB at version 4
        Mockito.when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        Mockito.when(preferencesRepository.findByUserId(1)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), null, 3L) // client sends 3
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
        Mockito.when(preferencesRepository.save(Mockito.any())).thenReturn(saved);

        // should not throw even though version=99 (no existing record means first save)
        UserPreferencesResponse result = service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), null, 99L)
        );

        assertThat(result).isNotNull();
    }

    @Test
    void saveForEmail_whenVersionIsNull_skipsCheck() {
        User user = userWithEmail("alice@example.com");
        UserPreferences existing = preferencesWithVersion(user, 4L);
        Mockito.when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));
        Mockito.when(preferencesRepository.findByUserId(1)).thenReturn(Optional.of(existing));
        Mockito.when(preferencesRepository.save(Mockito.any())).thenAnswer(inv -> inv.getArgument(0));

        // null version → skip check → should not throw
        service.saveForEmail(
                "alice@example.com",
                new UpdateUserPreferencesRequest(List.of(), List.of(), null, null)
        );

        Mockito.verify(preferencesRepository).save(existing);
    }
}
```

- [ ] **Run tests to verify they fail**

```bash
cd backend && ./gradlew test --tests "*.UserPreferencesServiceTest" 2>&1 | tail -15
# Expected: compilation failure or test failure — version check not yet implemented
```

- [ ] **Update `UserPreferencesService.saveForEmail()`** — add version check before modifying preferences:

```java
@Transactional
public UserPreferencesResponse saveForEmail(String email, UpdateUserPreferencesRequest request) {
    User user = requireUser(email);
    Optional<UserPreferences> existing = preferencesRepository.findByUserId(user.getId());
    UserPreferences preferences = existing.orElseGet(() -> createPreferences(user));

    if (existing.isPresent()
            && request.version() != null
            && !request.version().equals(preferences.getVersion())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Your preferences were modified elsewhere. Refresh and try again.");
    }

    preferences.setPreferredCategories(resolveCategories(request.categorySlugs()));
    preferences.setPreferredTags(resolveTags(request.tagSlugs()));
    preferences.setHomeCity(normalizeHomeCity(request.homeCity()));

    return UserPreferencesResponse.from(preferencesRepository.save(preferences));
}
```

- [ ] **Run unit tests**

```bash
cd backend && ./gradlew test --tests "*.UserPreferencesServiceTest" 2>&1 | tail -10
# Expected: BUILD SUCCESSFUL, 4 tests passed
```

- [ ] **Add integration test for 409 scenario** — add this test method to the existing `UserPreferencesIntegrationTest.java`:

```java
@Test
void staleSaveReturnsConflict() throws Exception {
    String token = registerAndGetToken("conflict-user@example.com");

    // First save — no version — creates preferences, response includes version=0
    String firstSaveBody = """
            { "categorySlugs": ["music"], "tagSlugs": [], "homeCity": "Vilnius" }
            """;
    String firstResponse = mvc.perform(put("/api/users/me/preferences")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(firstSaveBody))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(0))
            .andReturn().getResponse().getContentAsString();

    // Second save with correct version=0 — succeeds, version becomes 1
    mvc.perform(put("/api/users/me/preferences")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "categorySlugs": ["music"], "tagSlugs": [], "homeCity": "Kaunas", "version": 0 }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").value(1));

    // Third save with stale version=0 — returns 409
    mvc.perform(put("/api/users/me/preferences")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            { "categorySlugs": ["technology"], "tagSlugs": [], "homeCity": "Kaunas", "version": 0 }
                            """))
            .andExpect(status().isConflict());
}
```

- [ ] **Run full test suite**

```bash
cd backend && ./gradlew test 2>&1 | tail -15
# Expected: BUILD SUCCESSFUL
```

- [ ] **Commit**

```bash
git add backend/src/main/java/com/paradise/event_ticket_system/user/UserPreferencesService.java \
        backend/src/test/java/com/paradise/event_ticket_system/user/UserPreferencesServiceTest.java \
        backend/src/test/java/com/paradise/event_ticket_system/user/UserPreferencesIntegrationTest.java
git commit -m "feat: user preferences update with version check"
```

---

### Task 8: Frontend — attach `status` to API errors

**Files:**
- Modify: `frontend/src/api/client.js`

- [ ] **Update `apiFetch` error block** — attach `status` to every thrown error so call sites can check `err.status === 409` without a new class. Change lines 60-68 in `client.js`:

```js
if (!response.ok) {
  let message = 'Request failed';
  try {
    const body = await response.json();
    message = body.detail || body.message || body.error || message;
  } catch {
    message = response.statusText || message;
  }
  const err = new Error(message);
  err.status = response.status;
  throw err;
}
```

- [ ] **Commit**

```bash
git add frontend/src/api/client.js
git commit -m "feat: attach HTTP status code to API errors"
```

---

### Task 9: ConflictDialog component

**Files:**
- Create: `frontend/src/components/ConflictDialog.jsx`

- [ ] **Create the component** — uses the same Mantine components as `UserPreferencesModal.jsx`:

```jsx
import { Button, Group, Modal, Stack, Text } from "@mantine/core";

export default function ConflictDialog({ opened, onClose, onRefresh, onOverwrite }) {
  function handleRefresh() {
    onClose();
    onRefresh();
  }

  function handleOverwrite() {
    onClose();
    onOverwrite();
  }

  return (
    <Modal
      opened={opened}
      onClose={onClose}
      title="Edit conflict"
      size="sm"
      centered
    >
      <Stack gap="md">
        <Text size="sm">
          Someone else modified this record while you were editing. What would you like to do?
        </Text>
        <Group justify="flex-end" gap="sm">
          <Button variant="default" onClick={handleRefresh}>
            Refresh data
          </Button>
          <Button color="brand" onClick={handleOverwrite}>
            Force overwrite
          </Button>
        </Group>
      </Stack>
    </Modal>
  );
}
```

- [ ] **Commit**

```bash
git add frontend/src/components/ConflictDialog.jsx
git commit -m "feat: add ConflictDialog component"
```

---

### Task 10: Frontend API layer

**Files:**
- Modify: `frontend/src/api/eventsApi.js`
- Modify: `frontend/src/api/preferencesApi.js`

- [ ] **Update `updateEventStatus`** — add `version` parameter:

```js
export function updateEventStatus(organizerId, eventId, status, version) {
  return apiFetch(
    `/organizers/${organizerId}/events/${eventId}/status`,
    {
      method: "PATCH",
      body: JSON.stringify({ status, version }),
    }
  );
}
```

- [ ] **Update `saveMyPreferences`** — pass `version` from the preferences object:

```js
export async function saveMyPreferences(preferences) {
  return apiFetch("/users/me/preferences", {
    method: "PUT",
    body: JSON.stringify({
      categorySlugs: preferences.categorySlugs ?? [],
      tagSlugs: preferences.tagSlugs ?? [],
      homeCity: preferences.homeCity?.trim() || null,
      version: preferences.version ?? null,
    }),
  });
}
```

- [ ] **Commit**

```bash
git add frontend/src/api/eventsApi.js frontend/src/api/preferencesApi.js
git commit -m "feat: pass version in event status and preferences API calls"
```

---

### Task 11: OrganizerEventsPage conflict handling

**Files:**
- Modify: `frontend/src/pages/Organizer/OrganizerEventsPage.jsx`

- [ ] **Replace entire file** with conflict-aware version:

```jsx
import { useEffect, useState } from "react";
import { Card, Group, Select, Stack, Text, Title } from "@mantine/core";
import { getEvent, getEventsByOrganizerId, updateEventStatus } from "../../api/eventsApi.js";
import ConflictDialog from "../../components/ConflictDialog.jsx";

export default function OrganizerEventsPage({ organizerId }) {
  const [events, setEvents] = useState([]);
  const [conflictInfo, setConflictInfo] = useState(null);

  async function loadEvents() {
    try {
      const data = await getEventsByOrganizerId(organizerId);
      setEvents(data);
    } catch (err) {
      console.error(err);
    }
  }

  useEffect(() => {
    if (organizerId) loadEvents();
  }, [organizerId]);

  async function handleStatusChange(eventId, status, version) {
    // Optimistic update
    setEvents((prev) =>
      prev.map((e) => (e.id === eventId ? { ...e, status } : e))
    );

    try {
      await updateEventStatus(organizerId, eventId, status, version);
    } catch (err) {
      if (err.status === 409) {
        setConflictInfo({ eventId, intendedStatus: status });
        // Roll back optimistic update to show current server state
        loadEvents();
      } else {
        console.error("Backend update failed:", err);
        loadEvents();
      }
    }
  }

  async function handleConflictRefresh() {
    await loadEvents();
  }

  async function handleConflictOverwrite() {
    if (!conflictInfo) return;
    try {
      const freshEvent = await getEvent(conflictInfo.eventId);
      await updateEventStatus(
        organizerId,
        conflictInfo.eventId,
        conflictInfo.intendedStatus,
        freshEvent.version
      );
      await loadEvents();
    } catch (err) {
      console.error("Force overwrite failed:", err);
      await loadEvents();
    }
  }

  return (
    <>
      <Title mb="lg">My Events</Title>

      <ConflictDialog
        opened={conflictInfo !== null}
        onClose={() => setConflictInfo(null)}
        onRefresh={handleConflictRefresh}
        onOverwrite={handleConflictOverwrite}
      />

      <Stack>
        {events.map((event) => (
          <Card key={event.id} withBorder p="md" mb="sm">
            <Group justify="space-between" align="center">
              <div>
                <Text fw={600}>{event.title}</Text>
                <Text size="sm" c="dimmed">
                  {event.startDatetime}
                </Text>
              </div>
              <Select
                value={event.status}
                data={[
                  { value: "DRAFT", label: "Draft" },
                  { value: "PUBLISHED", label: "Published" },
                  { value: "CANCELED", label: "Canceled" },
                ]}
                onChange={(value) =>
                  handleStatusChange(event.id, value, event.version)
                }
                w={150}
              />
            </Group>
          </Card>
        ))}
      </Stack>
    </>
  );
}
```

- [ ] **Commit**

```bash
git add frontend/src/pages/Organizer/OrganizerEventsPage.jsx
git commit -m "feat: conflict detection and resolution in OrganizerEventsPage"
```

---

### Task 12: UserPreferencesModal conflict handling

**Files:**
- Modify: `frontend/src/components/UserPreferencesModal.jsx`

- [ ] **Replace entire file** with conflict-aware version:

```jsx
import {
  Alert,
  Button,
  Group,
  Modal,
  Pill,
  Stack,
  Text,
  TextInput,
  Title,
} from "@mantine/core";
import { useEffect, useState } from "react";
import { getCatalog } from "../api/catalog.js";
import { getMyPreferences, saveMyPreferences } from "../api/preferencesApi.js";
import ConflictDialog from "./ConflictDialog.jsx";

function normalizeSlug(value) {
  return String(value ?? "").trim().toLowerCase();
}

function categorySlugFromOption(category) {
  return normalizeSlug(category.value);
}

export default function UserPreferencesModal({ opened, onClose, onSaved }) {
  const [catalog, setCatalog] = useState({ categories: [], tags: [] });
  const [categorySlugs, setCategorySlugs] = useState([]);
  const [tagSlugs, setTagSlugs] = useState([]);
  const [homeCity, setHomeCity] = useState("");
  const [version, setVersion] = useState(null);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [conflictOpened, setConflictOpened] = useState(false);

  useEffect(() => {
    if (!opened) {
      return undefined;
    }

    let cancelled = false;

    async function load() {
      setLoading(true);
      setError("");
      try {
        const [catalogData, preferences] = await Promise.all([
          getCatalog(),
          getMyPreferences(),
        ]);
        if (cancelled) {
          return;
        }
        setCatalog(catalogData);
        setCategorySlugs(preferences.categorySlugs ?? []);
        setTagSlugs(preferences.tagSlugs ?? []);
        setHomeCity(preferences.homeCity ?? "");
        setVersion(preferences.version ?? null);
      } catch (err) {
        if (!cancelled) {
          setError(err.message || "Could not load preferences");
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [opened]);

  function toggleCategory(slug) {
    setCategorySlugs((current) =>
      current.includes(slug)
        ? current.filter((value) => value !== slug)
        : [...current, slug]
    );
  }

  function toggleTag(slug) {
    setTagSlugs((current) =>
      current.includes(slug)
        ? current.filter((value) => value !== slug)
        : [...current, slug]
    );
  }

  async function handleSave() {
    setSaving(true);
    setError("");
    try {
      await saveMyPreferences({ categorySlugs, tagSlugs, homeCity, version });
      onSaved?.();
      onClose();
    } catch (err) {
      if (err.status === 409) {
        setConflictOpened(true);
      } else {
        setError(err.message || "Could not save preferences");
      }
    } finally {
      setSaving(false);
    }
  }

  async function handleConflictRefresh() {
    setLoading(true);
    setError("");
    try {
      const preferences = await getMyPreferences();
      setCategorySlugs(preferences.categorySlugs ?? []);
      setTagSlugs(preferences.tagSlugs ?? []);
      setHomeCity(preferences.homeCity ?? "");
      setVersion(preferences.version ?? null);
    } catch (err) {
      setError(err.message || "Could not reload preferences");
    } finally {
      setLoading(false);
    }
  }

  async function handleConflictOverwrite() {
    setSaving(true);
    setError("");
    try {
      const freshPrefs = await getMyPreferences();
      await saveMyPreferences({
        categorySlugs,
        tagSlugs,
        homeCity,
        version: freshPrefs.version,
      });
      onSaved?.();
      onClose();
    } catch (err) {
      setError(err.message || "Could not save preferences");
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <Modal
        opened={opened}
        onClose={onClose}
        title="Your recommendation preferences"
        size="lg"
        centered
        classNames={{ content: "preferences-modal" }}
      >
        <Stack gap="md">
          <Text size="sm" c="dimmed">
            These choices personalize the &quot;Recommended for you&quot; section. Browse filters on
            the left are separate and apply to the full list.
          </Text>

          <Stack gap="xs">
            <Title order={4} size="h5" c="brand.9">
              Favorite categories
            </Title>
            <Group gap="xs">
              {(catalog.categories ?? []).map((category) => {
                const slug = categorySlugFromOption(category);
                return (
                  <Pill
                    key={slug}
                    size="sm"
                    style={{ cursor: loading ? "default" : "pointer" }}
                    onClick={() => !loading && toggleCategory(slug)}
                    bg={
                      categorySlugs.includes(slug)
                        ? "var(--mantine-color-brand-1)"
                        : undefined
                    }
                  >
                    {category.label}
                  </Pill>
                );
              })}
            </Group>
          </Stack>

          <Stack gap="xs">
            <Title order={4} size="h5" c="brand.9">
              Interest tags
            </Title>
            <Group gap="xs">
              {(catalog.tags ?? []).map((tag) => {
                const slug = normalizeSlug(tag.slug);
                return (
                  <Pill
                    key={slug}
                    size="sm"
                    style={{ cursor: loading ? "default" : "pointer" }}
                    onClick={() => !loading && toggleTag(slug)}
                    bg={
                      tagSlugs.includes(slug)
                        ? "var(--mantine-color-brand-1)"
                        : undefined
                    }
                  >
                    {tag.label}
                  </Pill>
                );
              })}
            </Group>
          </Stack>

          <TextInput
            label="Home city"
            description="Events in this city score higher in recommendations"
            placeholder="e.g. Vilnius"
            value={homeCity}
            onChange={(event) => setHomeCity(event.currentTarget.value)}
            disabled={loading}
          />

          {error && (
            <Alert color="red" variant="light">
              {error}
            </Alert>
          )}

          <Group justify="flex-end" gap="sm">
            <Button variant="default" color="gray" onClick={onClose} disabled={saving}>
              Cancel
            </Button>
            <Button
              color="brand"
              onClick={handleSave}
              loading={saving}
              disabled={loading}
            >
              Save preferences
            </Button>
          </Group>
        </Stack>
      </Modal>

      {/* Rendered after the preferences Modal so Mantine's portal stacking places it on top */}
      <ConflictDialog
        opened={conflictOpened}
        onClose={() => setConflictOpened(false)}
        onRefresh={handleConflictRefresh}
        onOverwrite={handleConflictOverwrite}
      />
    </>
  );
}
```

- [ ] **Commit**

```bash
git add frontend/src/components/UserPreferencesModal.jsx
git commit -m "feat: conflict detection and resolution in UserPreferencesModal"
```

---

### Task 13: End-to-end smoke test

- [ ] **Start the backend**

```bash
cd backend && ./gradlew bootRun --args='--spring.profiles.active=seed' 2>&1 | grep "Started\|ERROR"
# Expected: Started EventTicketSystemApplication
```

- [ ] **Start the frontend**

```bash
cd frontend && npm run dev
# Open http://127.0.0.1:5173
```

- [ ] **Test conflict dialog — UserPreferences**
  1. Open the app in two browser tabs, log in with the same account in both
  2. Open "Preferences" modal in Tab 1 — note the version in network DevTools response (`version: 0`)
  3. Open "Preferences" modal in Tab 2 — save different preferences → observe version increments to 1
  4. Back in Tab 1 — click "Save preferences" → expect the ConflictDialog to appear
  5. Click "Refresh data" → form updates to server's current state, dialog closes
  6. Open preferences again in Tab 1, save → open Tab 2 and save stale version → click "Force overwrite" → Tab 1's data wins

- [ ] **Test conflict dialog — OrganizerEventsPage**
  1. Log in as an organizer in two tabs
  2. Tab 1: change event status → succeeds
  3. Tab 2: load events (old version), Tab 1: change status again → Tab 2 status dropdown has stale version
  4. Tab 2: try to change status → ConflictDialog appears
  5. Click "Refresh data" → events reload, dialog closes
  6. Try "Force overwrite" → status saves successfully using fresh version

- [ ] **Final commit**

```bash
git add -A
git status  # should be clean
```
