package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.ticket.TicketType;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import com.paradise.event_ticket_system.ticket.TicketTypeResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public EventController(EventRepository eventRepository, TicketTypeRepository ticketTypeRepository) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @GetMapping("/{eventId}")
    public EventResponse getEvent(@PathVariable Long eventId) {
        return eventRepository.findById(eventId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    @GetMapping("/{eventId}/ticket-types")
    public List<TicketTypeResponse> getTicketTypes(@PathVariable Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return ticketTypeRepository.findByEventIdOrderById(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    private EventResponse toResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                event.getVenueName(),
                event.getAddress(),
                event.getCity(),
                event.getCountry(),
                event.isSalesEnabled()
        );
    }

    private TicketTypeResponse toResponse(TicketType ticketType) {
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getAvailableQuantity(),
                ticketType.isSalesEnabled()
        );
    }
}
