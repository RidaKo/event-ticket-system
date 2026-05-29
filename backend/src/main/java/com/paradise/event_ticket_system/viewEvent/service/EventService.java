package com.paradise.event_ticket_system.viewEvent.service;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.category.CategoryRepository;
import com.paradise.event_ticket_system.config.EditConflictException;
import com.paradise.event_ticket_system.event.EventStatus;
import com.paradise.event_ticket_system.model.*;
import com.paradise.event_ticket_system.viewEvent.api.DTO.EventRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.EventResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.EventStatusConflictResponse;
import com.paradise.event_ticket_system.viewEvent.api.EventMapper;
import com.paradise.event_ticket_system.viewEvent.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AuditedBusinessAction
@RequiredArgsConstructor
public class EventService {

    private static final int MAX_EVENT_PAGE_SIZE = 50;

    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;
    private final VenueRepository venueRepository;
    private final OrganizerRepository organizerRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Transactional(readOnly = true)
    public EventResponse getEventById(Integer id) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event with id " + id + " not found"
                ));
        List<Review> reviews = reviewRepository.findByEventIdWithUser(id);
        return eventMapper.toEventResponse(event, reviews);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return getAllEvents(0, MAX_EVENT_PAGE_SIZE);
    }

    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents(int page, int size) {
        int effectivePage = Math.max(0, page);
        int effectiveSize = Math.min(Math.max(1, size), MAX_EVENT_PAGE_SIZE);
        return eventRepository.findAllWithDetails(PageRequest.of(effectivePage, effectiveSize)).stream()
                .map(event -> {
                    List<Review> reviews = reviewRepository.findByEventIdWithUser(event.getId());
                    return eventMapper.toEventResponse(event, reviews);
                })
                .toList();
    }
    @Transactional
    public EventResponse createEvent(EventRequest request) {
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venue not found"));

        Organizer organizer = organizerRepository.findById(request.organizerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organizer not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Event event = eventMapper.toEventEntity(request, organizer, venue, category);
        Event saved = eventRepository.save(event);
        return eventMapper.toEventResponse(saved, List.of());
    }
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByOrganizerId(
            Integer organizerId
    ) {

        return eventRepository.findByOrganizerId(organizerId)
                .stream()
                .map(event -> {

                    List<Review> reviews =
                            reviewRepository.findByEventIdWithUser(
                                    event.getId()
                            );

                    return eventMapper.toEventResponse(
                            event,
                            reviews
                    );
                })
                .toList();
    }
    @Transactional
    public void updateStatus(
            Integer organizerId,
            Integer eventId,
            EventStatus status,
            Long version
    ) {
        updateStatus(organizerId, eventId, status, version, false);
    }

    @Transactional
    public void updateStatus(
            Integer organizerId,
            Integer eventId,
            EventStatus status,
            Long version,
            boolean force
    ) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }

        if (!force && (version == null || !version.equals(event.getVersion()))) {
            throw new EditConflictException(
                    "This event was modified by someone else. Refresh, compare, then retry or overwrite.",
                    eventStatusConflict(event));
        }

        event.setStatus(status);
    }

    private EventStatusConflictResponse eventStatusConflict(Event event) {
        return new EventStatusConflictResponse(event.getId(), event.getVersion(), event.getStatus());
    }
}
