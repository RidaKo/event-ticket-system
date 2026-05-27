package com.paradise.event_ticket_system.viewEvent.service;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.event.CheckoutCatalogRules;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import com.paradise.event_ticket_system.viewEvent.api.DTO.TicketTypeRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.TicketTypeResponse;
import com.paradise.event_ticket_system.viewEvent.domain.EventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@AuditedBusinessAction
@RequiredArgsConstructor
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<com.paradise.event_ticket_system.ticket.TicketTypeResponse> getCheckoutTicketTypesForEvent(
            Integer eventId
    ) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found");
        }
        List<TicketType> ticketTypes = ticketTypeRepository.findByEventIdOrderById(eventId);
        return ticketTypes.stream()
                .map(this::toCheckoutResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> getTicketTypesForEvent(Integer eventId) {
        List<TicketType> ticketTypes = ticketTypeRepository.findByEventIdOrderById(eventId);
        return ticketTypes.stream().map(this::toResponse).toList();
    }

    @Transactional
    public TicketTypeResponse createTicketType(Integer eventId, TicketTypeRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event with id " + eventId + " not found"
                ));

        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);
        ticketType.setName(request.name());
        ticketType.setDescription(request.description());
        ticketType.setPrice(request.price());
        ticketType.setCurrency(request.currency());
        ticketType.setQuantityTotal(request.quantityTotal());
        ticketType.setQuantitySold(0);
        ticketType.setSaleStart(request.saleStart());
        ticketType.setSaleEnd(request.saleEnd());
        ticketType.setMaxPerOrder(request.maxPerOrder());
        if (request.isActive() != null) {
            ticketType.setIsActive(request.isActive());
        }

        TicketType saved = ticketTypeRepository.save(ticketType);
        return toResponse(saved);
    }

    private TicketTypeResponse toResponse(TicketType ticketType) {
        int available = ticketType.getQuantityTotal() - ticketType.getQuantitySold();
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getEvent().getId(),
                ticketType.getName(),
                ticketType.getDescription(),
                ticketType.getPrice(),
                ticketType.getCurrency(),
                ticketType.getQuantityTotal(),
                ticketType.getQuantitySold(),
                Math.max(available, 0),
                ticketType.getSaleStart(),
                ticketType.getSaleEnd(),
                ticketType.getMaxPerOrder(),
                ticketType.getIsActive()
        );
    }

    private com.paradise.event_ticket_system.ticket.TicketTypeResponse toCheckoutResponse(TicketType ticketType) {
        return new com.paradise.event_ticket_system.ticket.TicketTypeResponse(
                ticketType.getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                CheckoutCatalogRules.availableQuantity(ticketType),
                CheckoutCatalogRules.isTicketSalesEnabled(ticketType)
        );
    }
}
