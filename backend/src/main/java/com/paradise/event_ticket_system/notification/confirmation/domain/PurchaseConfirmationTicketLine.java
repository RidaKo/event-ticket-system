package com.paradise.event_ticket_system.notification.confirmation.domain;

public record PurchaseConfirmationTicketLine(
	String ticketType,
	int quantity
) {
}
