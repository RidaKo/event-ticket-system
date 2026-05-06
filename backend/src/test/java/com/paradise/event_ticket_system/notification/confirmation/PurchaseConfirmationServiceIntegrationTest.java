package com.paradise.event_ticket_system.notification.confirmation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Order;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.notification.confirmation.api.PaymentStatus;
import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationOutcome;
import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationRequest;
import com.paradise.event_ticket_system.notification.confirmation.domain.EmailDeliveryStatus;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDeliveryRepository;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationDispatchResult;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationEmailMessage;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationEmailSender;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PurchaseConfirmationServiceIntegrationTest {

	@Autowired
	private PurchaseConfirmationService purchaseConfirmationService;

	@Autowired
	private PurchaseConfirmationDeliveryRepository deliveryRepository;

	@Autowired
	private CapturingPurchaseConfirmationEmailSender emailSender;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@PersistenceContext
	private EntityManager entityManager;

	@BeforeEach
	void setUp() {
		clearDatabase();
		emailSender.clear();
	}

	@Test
	void sendsConfirmationFromPersistedOrderAfterSuccessfulPayment() {
		OrderFixture fixture = createOrderFixture();

		PurchaseConfirmationDispatchResult result = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderId(), PaymentStatus.SUCCEEDED)
		);

		assertThat(result.outcome()).isEqualTo(PurchaseConfirmationOutcome.QUEUED);
		assertThat(result.alreadyProcessed()).isFalse();
		assertThat(result.orderId()).isEqualTo(fixture.orderId());
		assertThat(result.orderReference()).isEqualTo(fixture.orderReference());

		List<PurchaseConfirmationDelivery> deliveries = deliveryRepository.findAll();
		assertThat(deliveries).hasSize(1);

		PurchaseConfirmationDelivery delivery = deliveries.getFirst();
		assertThat(delivery.getOrderId()).isEqualTo(fixture.orderId());
		assertThat(delivery.getOrderReference()).isEqualTo(fixture.orderReference());
		assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryStatus.SENT);
		assertThat(delivery.getSentAt()).isNotNull();
		assertThat(delivery.getTotalQuantity()).isEqualTo(3);
		assertThat(delivery.getTicketLines())
			.extracting(ticketLine -> ticketLine.ticketType() + ":" + ticketLine.quantity())
			.containsExactly("General Admission:1", "VIP Ticket:2");

		assertThat(emailSender.messages()).hasSize(1);
		PurchaseConfirmationEmailMessage message = emailSender.messages().getFirst();
		assertThat(message.subject()).contains(fixture.eventTitle(), fixture.orderReference());
		assertThat(message.htmlBody()).contains("General Admission");
		assertThat(message.htmlBody()).contains("VIP Ticket");
		assertThat(message.textBody()).contains("- General Admission x 1");
		assertThat(message.textBody()).contains("- VIP Ticket x 2");
		assertThat(message.textBody()).contains("Total quantity: 3");
	}

	@Test
	void doesNotSendEmailWhenPaymentFails() {
		OrderFixture fixture = createOrderFixture();

		PurchaseConfirmationDispatchResult result = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderId(), PaymentStatus.FAILED)
		);

		assertThat(result.outcome()).isEqualTo(PurchaseConfirmationOutcome.SKIPPED);
		assertThat(result.orderId()).isEqualTo(fixture.orderId());
		assertThat(result.orderReference()).isEqualTo(fixture.orderReference());
		assertThat(deliveryRepository.findAll()).isEmpty();
		assertThat(emailSender.messages()).isEmpty();
	}

	@Test
	void preventsDuplicateEmailSendsForSameOrder() {
		OrderFixture fixture = createOrderFixture();

		PurchaseConfirmationDispatchResult first = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderId(), PaymentStatus.SUCCEEDED)
		);
		PurchaseConfirmationDispatchResult second = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderId(), PaymentStatus.SUCCEEDED)
		);

		assertThat(first.outcome()).isEqualTo(PurchaseConfirmationOutcome.QUEUED);
		assertThat(second.outcome()).isEqualTo(PurchaseConfirmationOutcome.ALREADY_PROCESSED);
		assertThat(second.alreadyProcessed()).isTrue();
		assertThat(deliveryRepository.findAll()).hasSize(1);
		assertThat(emailSender.messages()).hasSize(1);
	}

	private OrderFixture createOrderFixture() {
		return transactionTemplate.execute(status -> {
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
			entityManager.clear();

			return new OrderFixture(order.getId(), order.getPaymentReference(), event.getTitle());
		});
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

	private void clearDatabase() {
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
		for (String table : List.of(
			"purchase_confirmation_deliveries",
			"tickets",
			"orders",
			"ticket_types",
			"events",
			"venues",
			"organizers",
			"categories",
			"users"
		)) {
			jdbcTemplate.execute("DELETE FROM " + table);
		}
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
	}

	private record OrderFixture(Integer orderId, String orderReference, String eventTitle) {
	}

	@TestConfiguration
	static class TestEmailSenderConfiguration {

		@Bean
		@Primary
		CapturingPurchaseConfirmationEmailSender capturingPurchaseConfirmationEmailSender() {
			return new CapturingPurchaseConfirmationEmailSender();
		}
	}

	static class CapturingPurchaseConfirmationEmailSender implements PurchaseConfirmationEmailSender {

		private final List<PurchaseConfirmationEmailMessage> messages = new ArrayList<>();

		@Override
		public void send(PurchaseConfirmationEmailMessage message) {
			messages.add(message);
		}

		List<PurchaseConfirmationEmailMessage> messages() {
			return messages;
		}

		void clear() {
			messages.clear();
		}
	}
}
