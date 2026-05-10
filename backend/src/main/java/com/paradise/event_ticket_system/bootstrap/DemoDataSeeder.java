package com.paradise.event_ticket_system.bootstrap;

import java.math.BigDecimal;
import java.time.Instant;

import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.OrderItem;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.Payment;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.payment.PaymentMethodType;
import com.paradise.event_ticket_system.payment.PaymentStatus;
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
		Long existingOrders = entityManager.createQuery("select count(o) from PurchaseOrder o", Long.class).getSingleResult();
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
		generalAdmission.setQuantitySold(0);
		entityManager.persist(generalAdmission);

		TicketType vipTicket = new TicketType();
		vipTicket.setEvent(event);
		vipTicket.setName("VIP Ticket");
		vipTicket.setPrice(new BigDecimal("150.00"));
		vipTicket.setCurrency("USD");
		vipTicket.setQuantityTotal(50);
		vipTicket.setQuantitySold(0);
		entityManager.persist(vipTicket);

		PurchaseOrder order = new PurchaseOrder();
		order.setOrderNumber("ORD-2026-04-8472");
		order.setEvent(event);
		order.setGuestName("Alex Buyer");
		order.setGuestEmail("attendee@example.com");
		order.setStatus(OrderStatus.CONFIRMED);
		order.setSubtotal(new BigDecimal("385.00"));
		order.setDiscountAmount(BigDecimal.ZERO);
		order.setTotalAmount(new BigDecimal("385.00"));
		order.addItem(orderItem(generalAdmission, "General Admission", "85.00", 1));
		order.addItem(orderItem(vipTicket, "VIP Ticket", "150.00", 2));
		entityManager.persist(order);

		Payment payment = new Payment();
		payment.setOrder(order);
		payment.setAmount(order.getTotalAmount());
		payment.setMethodType(PaymentMethodType.CARD);
		payment.setProviderReference("mock_seed_payment");
		payment.setCardLast4("4242");
		payment.setStatus(PaymentStatus.SUCCEEDED);
		order.setPayment(payment);
		entityManager.persist(payment);

		entityManager.flush();

		log.info(
			"Seeded demo order data. orderId={}, orderNumber={}, testPayload={{\"orderNumber\":\"{}\"}}",
			order.getId(),
			order.getOrderNumber(),
			order.getOrderNumber()
		);
	}

	private OrderItem orderItem(TicketType ticketType, String ticketName, String unitPrice, int quantity) {
		OrderItem item = new OrderItem();
		item.setTicketType(ticketType);
		item.setTicketName(ticketName);
		item.setUnitPrice(new BigDecimal(unitPrice));
		item.setQuantity(quantity);
		item.setLineTotal(new BigDecimal(unitPrice).multiply(BigDecimal.valueOf(quantity)));
		return item;
	}
}
