package com.paradise.event_ticket_system.notification.confirmation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.paradise.event_ticket_system.checkout.CheckoutService;
import com.paradise.event_ticket_system.checkout.CreateOrderRequest;
import com.paradise.event_ticket_system.checkout.GuestCreateOrderRequest;
import com.paradise.event_ticket_system.checkout.GuestOrderCreatedResponse;
import com.paradise.event_ticket_system.checkout.OrderResponse;
import com.paradise.event_ticket_system.checkout.PaymentRequest;
import com.paradise.event_ticket_system.checkout.PaymentResponse;
import com.paradise.event_ticket_system.checkout.TicketItemRequest;
import com.paradise.event_ticket_system.event.EventStatus;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.OrderItem;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.Payment;
import com.paradise.event_ticket_system.model.EmailDeliveryStatus;
import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationOutcome;
import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationRequest;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDeliveryRepository;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationDispatchResult;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationEmailMessage;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationEmailSender;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationService;
import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.payment.PaymentMethodType;
import com.paradise.event_ticket_system.payment.PaymentStatus;
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
	private CheckoutService checkoutService;

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
	void sendsConfirmationFromPersistedPurchaseOrderAfterSuccessfulPayment() {
		OrderFixture fixture = createPurchaseOrderFixture(OrderStatus.CONFIRMED, PaymentStatus.SUCCEEDED);

		PurchaseConfirmationDispatchResult result = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderNumber())
		);

		assertThat(result.outcome()).isEqualTo(PurchaseConfirmationOutcome.QUEUED);
		assertThat(result.alreadyProcessed()).isFalse();
		assertThat(result.orderId()).isEqualTo(fixture.orderId());
		assertThat(result.orderReference()).isEqualTo(fixture.orderNumber());

		List<PurchaseConfirmationDelivery> deliveries = deliveryRepository.findAll();
		assertThat(deliveries).hasSize(1);

		PurchaseConfirmationDelivery delivery = deliveries.getFirst();
		assertThat(delivery.getOrderId()).isEqualTo(fixture.orderId());
		assertThat(delivery.getOrderReference()).isEqualTo(fixture.orderNumber());
		assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryStatus.SENT);
		assertThat(delivery.getSentAt()).isNotNull();
		assertThat(delivery.getTotalQuantity()).isEqualTo(3);
		assertThat(delivery.getTicketLines())
			.extracting(ticketLine -> ticketLine.ticketType() + ":" + ticketLine.quantity())
			.containsExactly("General Admission:1", "VIP Ticket:2");

		assertThat(emailSender.messages()).hasSize(1);
		PurchaseConfirmationEmailMessage message = emailSender.messages().getFirst();
		assertThat(message.subject()).contains(fixture.eventTitle(), fixture.orderNumber());
		assertThat(message.htmlBody()).contains("General Admission");
		assertThat(message.htmlBody()).contains("VIP Ticket");
		assertThat(message.textBody()).contains("- General Admission x 1");
		assertThat(message.textBody()).contains("- VIP Ticket x 2");
		assertThat(message.textBody()).contains("Total quantity: 3");
		assertThat(delivery.getOrderAccessUrl()).contains(fixture.orderNumber());
		assertThat(message.inlineImages()).hasSize(3);
		assertThat(message.htmlBody()).contains("cid:ticket-");
		assertThat(message.htmlBody()).contains("TKT-");
	}

	@Test
	void doesNotSendEmailWhenPersistedPaymentFails() {
		OrderFixture fixture = createPurchaseOrderFixture(OrderStatus.PAYMENT_FAILED, PaymentStatus.FAILED);

		PurchaseConfirmationDispatchResult result = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderNumber())
		);

		assertThat(result.outcome()).isEqualTo(PurchaseConfirmationOutcome.SKIPPED);
		assertThat(result.orderId()).isEqualTo(fixture.orderId());
		assertThat(result.orderReference()).isEqualTo(fixture.orderNumber());
		assertThat(deliveryRepository.findAll()).isEmpty();
		assertThat(emailSender.messages()).isEmpty();
	}

	@Test
	void preventsDuplicateEmailSendsForSameOrder() {
		OrderFixture fixture = createPurchaseOrderFixture(OrderStatus.CONFIRMED, PaymentStatus.SUCCEEDED);

		PurchaseConfirmationDispatchResult first = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderNumber())
		);
		PurchaseConfirmationDispatchResult second = purchaseConfirmationService.handle(
			new PurchaseConfirmationRequest(fixture.orderNumber())
		);

		assertThat(first.outcome()).isEqualTo(PurchaseConfirmationOutcome.QUEUED);
		assertThat(second.outcome()).isEqualTo(PurchaseConfirmationOutcome.ALREADY_PROCESSED);
		assertThat(second.alreadyProcessed()).isTrue();
		assertThat(deliveryRepository.findAll()).hasSize(1);
		assertThat(emailSender.messages()).hasSize(1);
	}

	@Test
	void guestCheckoutSuccessfulPaymentQueuesPurchaseConfirmation() {
		CatalogFixture catalog = createCatalogFixture();

		GuestOrderCreatedResponse order = checkoutService.createGuestOrder(new GuestCreateOrderRequest(
			catalog.eventId(),
			"Alex Buyer",
			"attendee@example.com",
			null,
			List.of(
				new TicketItemRequest(catalog.generalAdmissionTicketTypeId(), 1),
				new TicketItemRequest(catalog.vipTicketTypeId(), 2)
			)
		));
		PaymentResponse payment = checkoutService.submitPayment(
			order.orderNumber(),
			new PaymentRequest(PaymentMethodType.CARD, "4242 4242 4242 4242")
		);

		assertThat(payment.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
		assertThat(payment.paymentStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
		assertThat(deliveryRepository.findAll()).hasSize(1);
		assertThat(emailSender.messages()).hasSize(1);
		assertThat(emailSender.messages().getFirst().subject()).contains(order.orderNumber());
	}

	@Test
	void authenticatedCheckoutSuccessfulPaymentQueuesPurchaseConfirmation() {
		CatalogFixture catalog = createCatalogFixture();
		createRegisteredUser("buyer@example.com", "Logged In Buyer");

		OrderResponse order = checkoutService.createOrderForUser(new CreateOrderRequest(
			catalog.eventId(),
			null,
			List.of(new TicketItemRequest(catalog.generalAdmissionTicketTypeId(), 1))
		), "buyer@example.com");
		PaymentResponse payment = checkoutService.submitPayment(
			order.orderNumber(),
			new PaymentRequest(PaymentMethodType.CARD, "4242 4242 4242 4242")
		);

		assertThat(payment.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
		assertThat(payment.paymentStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
		assertThat(deliveryRepository.findAll()).hasSize(1);
		assertThat(emailSender.messages()).hasSize(1);
		assertThat(emailSender.messages().getFirst().to()).isEqualTo("buyer@example.com");
		assertThat(emailSender.messages().getFirst().subject()).contains(order.orderNumber());
	}

	private OrderFixture createPurchaseOrderFixture(OrderStatus orderStatus, PaymentStatus paymentStatus) {
		return transactionTemplate.execute(status -> {
			CatalogFixture catalog = createCatalogFixtureInCurrentTransaction();
			Event event = entityManager.getReference(Event.class, catalog.eventId());
			TicketType generalAdmission = entityManager.getReference(TicketType.class, catalog.generalAdmissionTicketTypeId());
			TicketType vipTicket = entityManager.getReference(TicketType.class, catalog.vipTicketTypeId());

			PurchaseOrder order = new PurchaseOrder();
			order.setOrderNumber("ORD-2026-04-8472");
			order.setEvent(event);
			order.setGuestName("Alex Buyer");
			order.setGuestEmail("attendee@example.com");
			order.setStatus(orderStatus);
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
			payment.setProviderReference("mock_test_payment");
			payment.setCardLast4("4242");
			payment.setStatus(paymentStatus);
			order.setPayment(payment);
			entityManager.persist(payment);

			entityManager.flush();
			entityManager.clear();

			return new OrderFixture(order.getId(), order.getOrderNumber(), event.getTitle());
		});
	}

	private CatalogFixture createCatalogFixture() {
		return transactionTemplate.execute(status -> createCatalogFixtureInCurrentTransaction());
	}

	private void createRegisteredUser(String email, String fullName) {
		transactionTemplate.executeWithoutResult(status -> {
			User user = new User();
			user.setEmail(email);
			user.setFullName(fullName);
			user.setPasswordHash("registered-user-hash");
			user.setIsGuest(false);
			entityManager.persist(user);
		});
	}

	private CatalogFixture createCatalogFixtureInCurrentTransaction() {
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
		event.setStatus(EventStatus.PUBLISHED);
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
		generalAdmission.setMaxPerOrder(10);
		generalAdmission.setIsActive(true);
		entityManager.persist(generalAdmission);

		TicketType vipTicket = new TicketType();
		vipTicket.setEvent(event);
		vipTicket.setName("VIP Ticket");
		vipTicket.setPrice(new BigDecimal("150.00"));
		vipTicket.setCurrency("USD");
		vipTicket.setQuantityTotal(50);
		vipTicket.setQuantitySold(0);
		vipTicket.setMaxPerOrder(10);
		vipTicket.setIsActive(true);
		entityManager.persist(vipTicket);

		entityManager.flush();

		return new CatalogFixture(event.getId(), generalAdmission.getId(), vipTicket.getId());
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

	private void clearDatabase() {
		jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
		for (String table : List.of(
			"purchase_confirmation_deliveries",
			"payment",
			"order_items",
			"purchase_orders",
			"discount_code",
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

	private record CatalogFixture(Integer eventId, Integer generalAdmissionTicketTypeId, Integer vipTicketTypeId) {
	}

	private record OrderFixture(Long orderId, String orderNumber, String eventTitle) {
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
