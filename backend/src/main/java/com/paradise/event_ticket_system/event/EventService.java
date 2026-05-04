package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.event.dto.CreateEventRequest;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.Venue;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Owns the create/list logic behind {@code /api/events}. Looks up the demo
 * organizer by user id, reuses or provisions a venue by name + city, and
 * stamps a unique slug on every persisted event.
 */
@Service
public class EventService {

    private static final String DEFAULT_COUNTRY = "LT";
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    private static final Duration DEFAULT_DURATION = Duration.ofHours(3);

    private final OrganizerRepository organizerRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final EntityManager entityManager;

    public EventService(OrganizerRepository organizerRepository,
                        VenueRepository venueRepository,
                        CategoryRepository categoryRepository,
                        EventRepository eventRepository,
                        EntityManager entityManager) {
        this.organizerRepository = organizerRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<Event> listForUser(Integer userId) {
        Organizer organizer = requireOrganizer(userId);
        return eventRepository.findByOrganizerOrderByCreatedDesc(organizer.getId());
    }

    @Transactional
    public Event create(Integer userId, CreateEventRequest request) {
        Organizer organizer = requireOrganizer(userId);
        Category category = categoryRepository.findBySlug(request.categorySlug().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Unknown category: " + request.categorySlug()));

        String venueName = request.venueName().trim();
        if (venueName.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Venue name is required");
        }
        String city = request.city().trim();
        if (city.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "City is required");
        }
        Venue venue = venueRepository.findFirstByOrganizer_IdAndNameIgnoreCaseAndCityIgnoreCase(
                        organizer.getId(),
                        venueName,
                        city
                )
                .orElseGet(() -> createVenue(organizer, venueName, city));

        Instant start = LocalDateTime.of(request.date(), request.time())
                .atZone(DEFAULT_ZONE)
                .toInstant();
        Instant end = start.plus(DEFAULT_DURATION);

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setCategory(category);
        event.setTitle(request.title().trim());
        event.setSlug(uniqueSlug(request.title()));
        event.setDescription(request.description());
        event.setStatus(request.publish() ? EventStatus.PUBLISHED.name() : EventStatus.DRAFT.name());
        event.setStartDatetime(start);
        event.setEndDatetime(end);
        event.setTimezone(DEFAULT_ZONE.getId());

        return eventRepository.save(event);
    }

    private Organizer requireOrganizer(Integer userId) {
        if (userId == null) {
            throw new ResponseStatusException(NOT_FOUND, "No demo user is configured");
        }
        return organizerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "No organizer profile for user " + userId));
    }

    private Venue createVenue(Organizer organizer, String name, String city) {
        Venue venue = new Venue();
        venue.setOrganizer(organizer);
        venue.setName(name);
        venue.setAddressLine1(name);
        venue.setCity(city);
        venue.setCountry(DEFAULT_COUNTRY);
        entityManager.persist(venue);
        return venue;
    }

    /**
     * Slugs are unique-constrained, so suffix with millis to keep the API
     * idempotent on accidental retries with the same title.
     */
    private static String uniqueSlug(String title) {
        String base = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.isEmpty()) {
            base = "event";
        }
        return base + "-" + System.currentTimeMillis();
    }
}
