package com.paradise.event_ticket_system.bootstrap;

import java.math.BigDecimal;
import java.time.Instant;

import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Order;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.model.Venue;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "event-ticket.seed", name = "enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

	private final EntityManager entityManager;

	public DemoDataSeeder(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		Long existingOrders = entityManager.createQuery("select count(o) from Order o", Long.class).getSingleResult();
		if (existingOrders != null && existingOrders > 0) {
			log.info("Skipping demo seed because orders already exist.");
			return;
		}

		User organizerUser = new User();
		organizerUser.setEmail("organizer@example.com");
		organizerUser.setFullName("Event Organizer");
		organizerUser.setPasswordHash("organizer-hash");
		entityManager.persist(organizerUser);

		Organizer organizer = new Organizer();
		organizer.setUser(organizerUser);
		organizer.setBusinessName("Paradise Events");
		entityManager.persist(organizer);

		Category category = new Category();
		category.setName("Music");
		category.setSlug("music");
		entityManager.persist(category);

		Venue venue = new Venue();
		venue.setOrganizer(organizer);
		venue.setName("Central Park Arena");
		venue.setAddressLine1("123 Main Street");
		venue.setCity("New York");
		venue.setCountry("US");
		entityManager.persist(venue);

		Event event = new Event();
		event.setOrganizer(organizer);
		event.setVenue(venue);
		event.setCategory(category);
		event.setTitle("Spring Music Festival");
		event.setSlug("spring-music-festival");
		event.setStatus("PUBLISHED");
		event.setStartDatetime(Instant.parse("2026-06-20T23:00:00Z"));
		event.setEndDatetime(Instant.parse("2026-06-21T03:00:00Z"));
		event.setTimezone("America/New_York");
		entityManager.persist(event);

		TicketType generalAdmission = new TicketType();
		generalAdmission.setEvent(event);
		generalAdmission.setName("General Admission");
		generalAdmission.setPrice(new BigDecimal("85.00"));
		generalAdmission.setCurrency("USD");
		generalAdmission.setQuantityTotal(100);
		entityManager.persist(generalAdmission);

		TicketType vipTicket = new TicketType();
		vipTicket.setEvent(event);
		vipTicket.setName("VIP Ticket");
		vipTicket.setPrice(new BigDecimal("150.00"));
		vipTicket.setCurrency("USD");
		vipTicket.setQuantityTotal(50);
		entityManager.persist(vipTicket);

		User attendee = new User();
		attendee.setEmail("attendee@example.com");
		attendee.setFullName("Alex Buyer");
		attendee.setPasswordHash("buyer-hash");
		entityManager.persist(attendee);

		Order order = new Order();
		order.setUser(attendee);
		order.setStatus("CONFIRMED");
		order.setSubtotal(new BigDecimal("385.00"));
		order.setFees(new BigDecimal("8.50"));
		order.setTax(new BigDecimal("7.45"));
		order.setTotal(new BigDecimal("400.95"));
		order.setCurrency("USD");
		order.setPaymentProvider("stripe");
		order.setPaymentReference("ORD-2026-04-8472");
		entityManager.persist(order);

		entityManager.persist(ticket(order, event, generalAdmission, attendee, "GA-001", "https://cdn.example.com/qr/GA-001.png"));
		entityManager.persist(ticket(order, event, vipTicket, attendee, "VIP-001", "https://cdn.example.com/qr/VIP-001.png"));
		entityManager.persist(ticket(order, event, vipTicket, attendee, "VIP-002", "https://cdn.example.com/qr/VIP-002.png"));

		entityManager.flush();

		log.info(
			"Seeded demo order data. orderId={}, orderReference={}, testPayload={{\"orderId\":{},\"paymentStatus\":\"SUCCEEDED\"}}",
			order.getId(),
			order.getPaymentReference(),
			order.getId()
		);
	}

	private Ticket ticket(
		Order order,
		Event event,
		TicketType ticketType,
		User attendee,
		String ticketCode,
		String qrCodeUrl
	) {
		Ticket ticket = new Ticket();
		ticket.setOrder(order);
		ticket.setEvent(event);
		ticket.setTicketType(ticketType);
		ticket.setOwnerUser(attendee);
		ticket.setOwnerEmail(attendee.getEmail());
		ticket.setOwnerName(attendee.getFullName());
		ticket.setPricePaid(ticketType.getPrice());
		ticket.setTicketCode(ticketCode);
		ticket.setQrCodeUrl(qrCodeUrl);
		ticket.setStatus("READY");
		return ticket;
	}
}
