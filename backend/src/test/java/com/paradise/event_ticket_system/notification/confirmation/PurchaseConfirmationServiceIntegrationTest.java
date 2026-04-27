package com.paradise.event_ticket_system.notification.confirmation;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PurchaseConfirmationServiceIntegrationTest {

	@Autowired
	private PurchaseConfirmationService purchaseConfirmationService;

	@Autowired
	private PurchaseConfirmationDeliveryRepository deliveryRepository;

	@Autowired
	private CapturingPurchaseConfirmationEmailSender emailSender;

	@BeforeEach
	void setUp() {
		deliveryRepository.deleteAll();
		emailSender.clear();
	}

	@Test
	void sendsConfirmationAndStoresLinkedDeliveryAfterSuccessfulPayment() {
		PurchaseConfirmationRequest request = successfulRequest();

		PurchaseConfirmationDispatchResult result = purchaseConfirmationService.handle(request);

		assertThat(result.outcome()).isEqualTo(PurchaseConfirmationOutcome.QUEUED);
		assertThat(result.alreadyProcessed()).isFalse();

		List<PurchaseConfirmationDelivery> deliveries = deliveryRepository.findAll();
		assertThat(deliveries).hasSize(1);

		PurchaseConfirmationDelivery delivery = deliveries.getFirst();
		assertThat(delivery.getOrderId()).isEqualTo(request.orderId());
		assertThat(delivery.getOrderReference()).isEqualTo(request.orderReference());
		assertThat(delivery.getStatus()).isEqualTo(EmailDeliveryStatus.SENT);
		assertThat(delivery.getSentAt()).isNotNull();

		assertThat(emailSender.messages()).hasSize(1);
		PurchaseConfirmationEmailMessage message = emailSender.messages().getFirst();
		assertThat(message.subject()).contains(request.eventTitle(), request.orderReference());
		assertThat(message.htmlBody()).contains(request.eventTitle());
		assertThat(message.htmlBody()).contains(request.eventLocation());
		assertThat(message.htmlBody()).contains(request.ticketType());
		assertThat(message.htmlBody()).contains(request.orderReference());
		assertThat(message.textBody()).contains("Quantity: " + request.quantity());
		assertThat(message.textBody()).contains(request.ticketUrl());
	}

	@Test
	void doesNotSendEmailWhenPaymentFails() {
		PurchaseConfirmationRequest request = new PurchaseConfirmationRequest(
			"order-124",
			"PUR-124",
			"attendee@example.com",
			"Paradise Expo",
			OffsetDateTime.parse("2026-05-02T20:00:00+03:00"),
			"Expo Center",
			"Standard",
			2,
			PaymentStatus.FAILED,
			"https://tickets.example.com/order-124",
			null
		);

		PurchaseConfirmationDispatchResult result = purchaseConfirmationService.handle(request);

		assertThat(result.outcome()).isEqualTo(PurchaseConfirmationOutcome.SKIPPED);
		assertThat(deliveryRepository.findAll()).isEmpty();
		assertThat(emailSender.messages()).isEmpty();
	}

	@Test
	void preventsDuplicateEmailSendsForSameOrder() {
		PurchaseConfirmationRequest request = successfulRequest();

		PurchaseConfirmationDispatchResult first = purchaseConfirmationService.handle(request);
		PurchaseConfirmationDispatchResult second = purchaseConfirmationService.handle(request);

		assertThat(first.outcome()).isEqualTo(PurchaseConfirmationOutcome.QUEUED);
		assertThat(second.outcome()).isEqualTo(PurchaseConfirmationOutcome.ALREADY_PROCESSED);
		assertThat(second.alreadyProcessed()).isTrue();
		assertThat(deliveryRepository.findAll()).hasSize(1);
		assertThat(emailSender.messages()).hasSize(1);
	}

	private PurchaseConfirmationRequest successfulRequest() {
		return new PurchaseConfirmationRequest(
			"order-123",
			"PUR-123",
			"attendee@example.com",
			"Spring Music Festival",
			OffsetDateTime.parse("2026-05-01T19:30:00+03:00"),
			"Riverside Arena",
			"VIP",
			3,
			PaymentStatus.SUCCEEDED,
			"https://tickets.example.com/order-123",
			"https://tickets.example.com/order-123/qr.png"
		);
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
