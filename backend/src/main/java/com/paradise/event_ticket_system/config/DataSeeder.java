package com.paradise.event_ticket_system.config;

import com.paradise.event_ticket_system.discount.DiscountCode;
import com.paradise.event_ticket_system.discount.DiscountRepository;
import com.paradise.event_ticket_system.discount.DiscountType;
import com.paradise.event_ticket_system.event.EventRepository;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final DiscountRepository discountRepository;
    private final EntityManager entityManager;

    public DataSeeder(
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            DiscountRepository discountRepository,
            EntityManager entityManager
    ) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.discountRepository = discountRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            return;
        }

        User organizerUser = new User();
        organizerUser.setEmail("organizer@example.com");
        organizerUser.setFullName("Paradise Events");
        entityManager.persist(organizerUser);

        Organizer organizer = new Organizer();
        organizer.setUser(organizerUser);
        organizer.setBusinessName("Paradise Events");
        organizer.setDescription("Local sample organizer for checkout development.");
        organizer.setVerified(true);
        entityManager.persist(organizer);

        Venue venue = new Venue();
        venue.setOrganizer(organizer);
        venue.setName("Central Park Amphitheater");
        venue.setAddressLine1("123 Park Avenue");
        venue.setCity("New York");
        venue.setCountry("US");
        entityManager.persist(venue);

        Category category = new Category();
        category.setName("Music");
        category.setSlug("music");
        entityManager.persist(category);

        Event event = new Event();
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setCategory(category);
        event.setTitle("Music Festival 2026");
        event.setSlug("music-festival-2026");
        event.setDescription("A summer music festival with general, VIP, and student tickets.");
        event.setStatus("PUBLISHED");
        event.setStartDatetime(Instant.parse("2026-06-20T16:00:00Z"));
        event.setEndDatetime(Instant.parse("2026-06-20T23:00:00Z"));
        event.setTimezone("America/New_York");
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
        ticketType.setCurrency("USD");
        ticketType.setQuantityTotal(quantity);
        ticketType.setQuantitySold(0);
        ticketType.setIsActive(true);
        ticketTypeRepository.save(ticketType);
    }
}
