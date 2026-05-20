package com.paradise.event_ticket_system.model;

public record PurchaseConfirmationTicketLine(
	String ticketType,
	int quantity
) {
}
