package com.paradise.event_ticket_system.viewEvent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paradise.event_ticket_system.category.CategoryRepository;
import com.paradise.event_ticket_system.config.EditConflictException;
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
        Event event = eventWithVersion(1, 6L);
        Mockito.when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.updateStatus(1, 1, EventStatus.PUBLISHED, 5L))
                .isInstanceOf(EditConflictException.class);
    }

    @Test
    void updateStatus_whenVersionIsNull_throwsConflict() {
        Event event = eventWithVersion(1, 6L);
        Mockito.when(eventRepository.findById(1)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.updateStatus(1, 1, EventStatus.CANCELED, null))
                .isInstanceOf(EditConflictException.class);

        assertThat(event.getStatus()).isNotEqualTo(EventStatus.CANCELED);
    }
}
