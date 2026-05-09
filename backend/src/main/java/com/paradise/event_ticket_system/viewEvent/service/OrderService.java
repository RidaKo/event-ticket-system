package com.paradise.event_ticket_system.viewEvent.service;

import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Order;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import com.paradise.event_ticket_system.viewEvent.api.DTO.OrderItemRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.OrderLineResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.OrderRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.OrderResponse;
import com.paradise.event_ticket_system.viewEvent.domain.EventRepository;
import com.paradise.event_ticket_system.viewEvent.domain.OrderRepository;
import com.paradise.event_ticket_system.viewEvent.domain.TicketRepository;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Event with id " + request.eventId() + " not found"
                ));

        User user = null;
        if (request.userId() != null) {
            user = userRepository.findById(request.userId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "User with id " + request.userId() + " not found"
                    ));
        } else {
            validateGuestInfo(request);
        }

        Map<Integer, Integer> requestedQuantities = mergeRequestedQuantities(request.items());
        List<TicketType> ticketTypes = ticketTypeRepository.findAllById(requestedQuantities.keySet());
        if (ticketTypes.size() != requestedQuantities.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more ticket types not found");
        }

        String currency = null;
        Instant now = Instant.now();
        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderLineResponse> lines = new ArrayList<>();

        for (TicketType ticketType : ticketTypes) {
            Integer quantity = requestedQuantities.get(ticketType.getId());
            if (!ticketType.getEvent().getId().equals(event.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket type does not match event");
            }
            validateTicketType(ticketType, quantity, now);

            if (currency == null) {
                currency = ticketType.getCurrency();
            } else if (!currency.equals(ticketType.getCurrency())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All ticket types must use the same currency");
            }

            BigDecimal lineTotal = ticketType.getPrice().multiply(BigDecimal.valueOf(quantity));
            subtotal = subtotal.add(lineTotal);
            lines.add(new OrderLineResponse(
                    ticketType.getId(),
                    ticketType.getName(),
                    quantity,
                    ticketType.getPrice(),
                    lineTotal
            ));
        }

        if (currency == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No ticket types selected");
        }

        Order order = new Order();
        order.setUser(user);
        order.setGuestEmail(user == null ? request.guestEmail() : user.getEmail());
        order.setGuestName(user == null ? request.guestName() : user.getFullName());
        order.setGuestPhone(request.guestPhone());
        order.setStatus("CONFIRMED");
        order.setSubtotal(subtotal);
        order.setFees(BigDecimal.ZERO);
        order.setTax(BigDecimal.ZERO);
        order.setTotal(subtotal);
        order.setCurrency(currency);

        Order savedOrder = orderRepository.save(order);

        List<Ticket> tickets = new ArrayList<>();
        for (TicketType ticketType : ticketTypes) {
            Integer quantity = requestedQuantities.get(ticketType.getId());
            ticketType.setQuantitySold(ticketType.getQuantitySold() + quantity);

            for (int i = 0; i < quantity; i++) {
                Ticket ticket = new Ticket();
                ticket.setOrder(savedOrder);
                ticket.setEvent(event);
                ticket.setTicketType(ticketType);
                ticket.setOwnerUser(user);
                ticket.setOwnerEmail(savedOrder.getGuestEmail());
                ticket.setOwnerName(savedOrder.getGuestName());
                ticket.setPricePaid(ticketType.getPrice());
                ticket.setTicketCode(UUID.randomUUID().toString());
                ticket.setStatus("ISSUED");
                tickets.add(ticket);
            }
        }

        ticketTypeRepository.saveAll(ticketTypes);
        ticketRepository.saveAll(tickets);

        return new OrderResponse(
                savedOrder.getId(),
                event.getId(),
                savedOrder.getStatus(),
                savedOrder.getSubtotal(),
                savedOrder.getFees(),
                savedOrder.getTax(),
                savedOrder.getTotal(),
                savedOrder.getCurrency(),
                lines
        );
    }

    private void validateGuestInfo(OrderRequest request) {
        if (request.guestEmail() == null || request.guestEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest email is required");
        }
        if (request.guestName() == null || request.guestName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest name is required");
        }
    }

    private Map<Integer, Integer> mergeRequestedQuantities(List<OrderItemRequest> items) {
        Map<Integer, Integer> merged = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            if (item.quantity() == null || item.quantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
            }
            merged.merge(item.ticketTypeId(), item.quantity(), Integer::sum);
        }
        return merged;
    }

    private void validateTicketType(TicketType ticketType, Integer quantity, Instant now) {
        if (Boolean.FALSE.equals(ticketType.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket type is inactive");
        }
        if (ticketType.getSaleStart() != null && now.isBefore(ticketType.getSaleStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket sales have not started");
        }
        if (ticketType.getSaleEnd() != null && now.isAfter(ticketType.getSaleEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket sales have ended");
        }
        if (ticketType.getMaxPerOrder() != null && quantity > ticketType.getMaxPerOrder()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity exceeds max per order");
        }
        int available = ticketType.getQuantityTotal() - ticketType.getQuantitySold();
        if (available < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not enough tickets available");
        }
    }
}
