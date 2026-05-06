package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationOutcome;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDelivery;

public record PurchaseConfirmationDispatchResult(
	Long deliveryId,
	Integer orderId,
	String orderReference,
	PurchaseConfirmationOutcome outcome,
	boolean alreadyProcessed
) {

	public static PurchaseConfirmationDispatchResult queued(PurchaseConfirmationDelivery delivery) {
		return new PurchaseConfirmationDispatchResult(
			delivery.getId(),
			delivery.getOrderId(),
			delivery.getOrderReference(),
			PurchaseConfirmationOutcome.QUEUED,
			false
		);
	}

	public static PurchaseConfirmationDispatchResult alreadyProcessed(PurchaseConfirmationDelivery delivery) {
		return new PurchaseConfirmationDispatchResult(
			delivery.getId(),
			delivery.getOrderId(),
			delivery.getOrderReference(),
			PurchaseConfirmationOutcome.ALREADY_PROCESSED,
			true
		);
	}

	public static PurchaseConfirmationDispatchResult skipped(Integer orderId, String orderReference) {
		return new PurchaseConfirmationDispatchResult(
			null,
			orderId,
			orderReference,
			PurchaseConfirmationOutcome.SKIPPED,
			false
		);
	}
}
