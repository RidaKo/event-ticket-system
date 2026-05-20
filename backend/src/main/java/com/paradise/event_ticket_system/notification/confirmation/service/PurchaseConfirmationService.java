package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationRequest;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDeliveryRepository;
import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.order.PurchaseOrderRepository;
import com.paradise.event_ticket_system.payment.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseConfirmationService {

	private static final Logger log = LoggerFactory.getLogger(PurchaseConfirmationService.class);

	private final PurchaseOrderRepository orderRepository;
	private final PurchaseConfirmationDeliveryRepository deliveryRepository;
	private final PurchaseConfirmationDeliveryFactory deliveryFactory;
	private final ApplicationEventPublisher eventPublisher;

	public PurchaseConfirmationService(
		PurchaseOrderRepository orderRepository,
		PurchaseConfirmationDeliveryRepository deliveryRepository,
		PurchaseConfirmationDeliveryFactory deliveryFactory,
		ApplicationEventPublisher eventPublisher
	) {
		this.orderRepository = orderRepository;
		this.deliveryRepository = deliveryRepository;
		this.deliveryFactory = deliveryFactory;
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public PurchaseConfirmationDispatchResult handle(PurchaseConfirmationRequest request) {
		PurchaseOrder order = orderRepository.findByOrderNumber(request.orderNumber())
			.orElseThrow(() -> new IllegalArgumentException("Unknown order number: " + request.orderNumber()));

		PurchaseConfirmationDelivery existingDelivery = deliveryRepository.findByOrderId(order.getId())
			.orElse(null);
		if (existingDelivery != null) {
			return PurchaseConfirmationDispatchResult.alreadyProcessed(existingDelivery);
		}

		if (!isConfirmedAndPaid(order)) {
			return PurchaseConfirmationDispatchResult.skipped(order.getId(), order.getOrderNumber());
		}

		PurchaseConfirmationDelivery delivery = deliveryFactory.createPending(order);

		try {
			deliveryRepository.saveAndFlush(delivery);
		}
		catch (DataIntegrityViolationException ex) {
			log.warn(
				"Could not insert purchase confirmation delivery for order {}: {}",
				order.getId(),
				ex.getMessage()
			);
			PurchaseConfirmationDelivery duplicateDelivery = deliveryRepository.findByOrderId(order.getId())
				.orElseThrow(() -> ex);
			return PurchaseConfirmationDispatchResult.alreadyProcessed(duplicateDelivery);
		}

		eventPublisher.publishEvent(new PurchaseConfirmationQueuedEvent(delivery.getId()));
		return PurchaseConfirmationDispatchResult.queued(delivery);
	}

	private boolean isConfirmedAndPaid(PurchaseOrder order) {
		return order.getStatus() == OrderStatus.CONFIRMED
			&& order.getPayment() != null
			&& order.getPayment().getStatus() == PaymentStatus.SUCCEEDED;
	}
}
