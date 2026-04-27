package com.paradise.event_ticket_system.notification.confirmation.service;

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

	private final PurchaseConfirmationDeliveryRepository deliveryRepository;
	private final ApplicationEventPublisher eventPublisher;

	public PurchaseConfirmationService(
		PurchaseConfirmationDeliveryRepository deliveryRepository,
		ApplicationEventPublisher eventPublisher
	) {
		this.deliveryRepository = deliveryRepository;
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
			return PurchaseConfirmationDispatchResult.skipped(request.orderId(), request.orderReference());
		}

		PurchaseConfirmationDelivery delivery = PurchaseConfirmationDelivery.pending(request);

		try {
			deliveryRepository.saveAndFlush(delivery);
		}
		catch (DataIntegrityViolationException ex) {
			PurchaseConfirmationDelivery duplicateDelivery = deliveryRepository.findByOrderId(request.orderId())
				.orElseThrow(() -> ex);
			return PurchaseConfirmationDispatchResult.alreadyProcessed(duplicateDelivery);
		}

		eventPublisher.publishEvent(new PurchaseConfirmationQueuedEvent(delivery.getId()));
		return PurchaseConfirmationDispatchResult.queued(delivery);
	}
}
