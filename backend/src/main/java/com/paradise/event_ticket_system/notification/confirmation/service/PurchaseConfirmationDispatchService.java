package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.model.EmailDeliveryStatus;
import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuditedBusinessAction
public class PurchaseConfirmationDispatchService {

	private static final Logger log = LoggerFactory.getLogger(PurchaseConfirmationDispatchService.class);

	private final PurchaseConfirmationDeliveryRepository deliveryRepository;
	private final ThymeleafPurchaseConfirmationEmailContentFactory emailContentFactory;
	private final PurchaseConfirmationEmailSender emailSender;

	public PurchaseConfirmationDispatchService(
		PurchaseConfirmationDeliveryRepository deliveryRepository,
		ThymeleafPurchaseConfirmationEmailContentFactory emailContentFactory,
		PurchaseConfirmationEmailSender emailSender
	) {
		this.deliveryRepository = deliveryRepository;
		this.emailContentFactory = emailContentFactory;
		this.emailSender = emailSender;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void dispatch(Long deliveryId) {
		PurchaseConfirmationDelivery delivery = deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Unknown delivery id: " + deliveryId));

		if (delivery.getStatus() != EmailDeliveryStatus.PENDING) {
			return;
		}

		try {
			PurchaseConfirmationEmailMessage message = emailContentFactory.createMessage(delivery);
			emailSender.send(message);
			delivery.markSent();
		}
		catch (RuntimeException ex) {
			log.error("Failed to send purchase confirmation email for order {}", delivery.getOrderId(), ex);
			delivery.markFailed(ex.getMessage());
		}
	}
}
