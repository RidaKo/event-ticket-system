package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AuditedBusinessAction
public class PurchaseConfirmationDispatchService {

	private static final Logger log = LoggerFactory.getLogger(PurchaseConfirmationDispatchService.class);

	private final ThymeleafPurchaseConfirmationEmailContentFactory emailContentFactory;
	private final PurchaseConfirmationEmailSender emailSender;
	private final PurchaseConfirmationDeliveryStatusService deliveryStatusService;

	public PurchaseConfirmationDispatchService(
		ThymeleafPurchaseConfirmationEmailContentFactory emailContentFactory,
		PurchaseConfirmationEmailSender emailSender,
		PurchaseConfirmationDeliveryStatusService deliveryStatusService
	) {
		this.emailContentFactory = emailContentFactory;
		this.emailSender = emailSender;
		this.deliveryStatusService = deliveryStatusService;
	}

	public void dispatch(Long deliveryId) {
		PurchaseConfirmationEmailData data = emailContentFactory.loadPendingData(deliveryId)
			.orElse(null);
		if (data == null) {
			return;
		}

		try {
			PurchaseConfirmationEmailMessage message = emailContentFactory.createMessage(data);
			emailSender.send(message);
			deliveryStatusService.markSent(deliveryId);
		}
		catch (RuntimeException ex) {
			log.error("Failed to send purchase confirmation email for order {}", data.orderId(), ex);
			deliveryStatusService.markFailed(deliveryId, ex.getMessage());
		}
	}
}
