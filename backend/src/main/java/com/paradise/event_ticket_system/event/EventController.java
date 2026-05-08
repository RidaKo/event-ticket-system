package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import com.paradise.event_ticket_system.ticket.TicketTypeResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController("checkoutEventController")
@RequestMapping("/api/events")
public class EventController {

    private final CheckoutEventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;

    public EventController(CheckoutEventRepository eventRepository, TicketTypeRepository ticketTypeRepository) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
    }

    @GetMapping("/{eventId}/ticket-types")
    public List<TicketTypeResponse> getTicketTypes(@PathVariable Integer eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        return ticketTypeRepository.findByEventIdOrderById(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketTypeResponse toResponse(TicketType ticketType) {
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                CheckoutCatalogRules.availableQuantity(ticketType),
                CheckoutCatalogRules.isTicketSalesEnabled(ticketType)
        );
    }
}
