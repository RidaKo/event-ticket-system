package com.paradise.event_ticket_system.notification.confirmation.service;

public record PurchaseConfirmationEmailMessage(
	String to,
	String subject,
	String htmlBody,
	String textBody
) {
}
