package com.paradise.event_ticket_system.config;

import com.paradise.event_ticket_system.discount.DiscountCode;
import com.paradise.event_ticket_system.discount.DiscountRepository;
import com.paradise.event_ticket_system.discount.DiscountType;
import com.paradise.event_ticket_system.event.Event;
import com.paradise.event_ticket_system.event.EventRepository;
import com.paradise.event_ticket_system.ticket.TicketType;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final DiscountRepository discountRepository;

    public DataSeeder(
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            DiscountRepository discountRepository
    ) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.discountRepository = discountRepository;
    }

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            return;
        }

        Event event = new Event();
        event.setTitle("Music Festival 2026");
        event.setDescription("A summer music festival with general, VIP, and student tickets.");
        event.setStartsAt(LocalDateTime.of(2026, 6, 20, 16, 0));
        event.setEndsAt(LocalDateTime.of(2026, 6, 20, 23, 0));
        event.setVenueName("Central Park Amphitheater");
        event.setAddress("123 Park Avenue");
        event.setCity("New York, NY 10001");
        event.setCountry("United States");
        event.setSalesEnabled(true);
        eventRepository.save(event);

        createTicketType(event, "General Admission", "39.00", 250);
        createTicketType(event, "VIP Ticket", "150.00", 50);
        createTicketType(event, "Student Ticket", "30.00", 100);

        DiscountCode save10 = new DiscountCode();
        save10.setEvent(event);
        save10.setCode("SAVE10");
        save10.setType(DiscountType.PERCENT);
        save10.setValue(new BigDecimal("10.00"));
        save10.setActive(true);
        save10.setMaxRedemptions(100);
        discountRepository.save(save10);
    }

    private void createTicketType(Event event, String name, String price, int quantity) {
        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);
        ticketType.setName(name);
        ticketType.setPrice(new BigDecimal(price));
        ticketType.setTotalQuantity(quantity);
        ticketType.setSalesEnabled(true);
        ticketTypeRepository.save(ticketType);
    }
}
