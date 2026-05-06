package com.paradise.event_ticket_system.notification.confirmation.service;

import java.util.List;

import com.paradise.event_ticket_system.model.Order;
import com.paradise.event_ticket_system.model.OrderRepository;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.model.TicketRepository;
import com.paradise.event_ticket_system.notification.confirmation.api.PaymentStatus;
import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationRequest;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDeliveryRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseConfirmationService {

	private final OrderRepository orderRepository;
	private final TicketRepository ticketRepository;
	private final PurchaseConfirmationDeliveryRepository deliveryRepository;
	private final PurchaseConfirmationDeliveryFactory deliveryFactory;
	private final ApplicationEventPublisher eventPublisher;

	public PurchaseConfirmationService(
		OrderRepository orderRepository,
		TicketRepository ticketRepository,
		PurchaseConfirmationDeliveryRepository deliveryRepository,
		PurchaseConfirmationDeliveryFactory deliveryFactory,
		ApplicationEventPublisher eventPublisher
	) {
		this.orderRepository = orderRepository;
		this.ticketRepository = ticketRepository;
		this.deliveryRepository = deliveryRepository;
		this.deliveryFactory = deliveryFactory;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public PurchaseConfirmationDispatchResult handle(PurchaseConfirmationRequest request) {
		PurchaseConfirmationDelivery existingDelivery = deliveryRepository.findByOrderId(request.orderId())
			.orElse(null);
		if (existingDelivery != null) {
			return PurchaseConfirmationDispatchResult.alreadyProcessed(existingDelivery);
		}

		if (request.paymentStatus() != PaymentStatus.SUCCEEDED) {
			Order skippedOrder = orderRepository.findById(request.orderId())
				.orElseThrow(() -> new IllegalArgumentException("Unknown order id: " + request.orderId()));
			return PurchaseConfirmationDispatchResult.skipped(
				skippedOrder.getId(),
				resolveOrderReference(skippedOrder)
			);
		}

		Order order = orderRepository.findById(request.orderId())
			.orElseThrow(() -> new IllegalArgumentException("Unknown order id: " + request.orderId()));
		List<Ticket> tickets = ticketRepository.findAllForPurchaseConfirmation(order.getId());
		PurchaseConfirmationDelivery delivery = deliveryFactory.createPending(order, tickets);

		try {
			deliveryRepository.saveAndFlush(delivery);
		}
		catch (DataIntegrityViolationException ex) {
			PurchaseConfirmationDelivery duplicateDelivery = deliveryRepository.findByOrderId(order.getId())
				.orElseThrow(() -> ex);
			return PurchaseConfirmationDispatchResult.alreadyProcessed(duplicateDelivery);
		}

		eventPublisher.publishEvent(new PurchaseConfirmationQueuedEvent(delivery.getId()));
		return PurchaseConfirmationDispatchResult.queued(delivery);
	}

	private String resolveOrderReference(Order order) {
		if (order.getPaymentReference() != null && !order.getPaymentReference().isBlank()) {
			return order.getPaymentReference();
		}

		return "ORD-%s".formatted(order.getId());
	}
}
