package com.paradise.event_ticket_system.notification.confirmation.api;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.util.StringUtils;

public record PurchaseConfirmationRequest(
	@NotBlank String orderId,
	@NotBlank String orderReference,
	@Email @NotBlank String attendeeEmail,
	@NotBlank String eventTitle,
	@NotNull OffsetDateTime eventDateTime,
	@NotBlank String eventLocation,
	@NotBlank String ticketType,
	@Positive int quantity,
	@NotNull PaymentStatus paymentStatus,
	String ticketUrl,
	String qrCodeImageUrl
) {

	@AssertTrue(message = "Either ticketUrl or qrCodeImageUrl must be provided.")
	public boolean hasTicketAccessInfo() {
		return StringUtils.hasText(ticketUrl) || StringUtils.hasText(qrCodeImageUrl);
	}
}
