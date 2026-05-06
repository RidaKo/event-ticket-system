package com.paradise.event_ticket_system.notification.confirmation.api;

import jakarta.validation.constraints.NotNull;

public record PurchaseConfirmationRequest(
	@NotNull Integer orderId,
	@NotNull PaymentStatus paymentStatus
) {
}
