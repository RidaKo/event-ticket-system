package com.paradise.event_ticket_system.notification.confirmation.api;

public record PurchaseConfirmationResponse(
	Long deliveryId,
	Integer orderId,
	String orderReference,
	PurchaseConfirmationOutcome status,
	boolean alreadyProcessed
) {
}
