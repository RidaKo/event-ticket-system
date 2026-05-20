package com.paradise.event_ticket_system.notification.confirmation.api;

import jakarta.validation.constraints.NotBlank;

public record PurchaseConfirmationRequest(
	@NotBlank String orderNumber
) {
}
