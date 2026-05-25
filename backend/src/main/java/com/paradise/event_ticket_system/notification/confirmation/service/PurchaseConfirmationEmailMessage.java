package com.paradise.event_ticket_system.notification.confirmation.service;

import java.util.List;

public record PurchaseConfirmationEmailMessage(
	String to,
	String subject,
	String htmlBody,
	String textBody,
	List<EmailInlineImage> inlineImages
) {
	public PurchaseConfirmationEmailMessage {
		inlineImages = inlineImages == null ? List.of() : List.copyOf(inlineImages);
	}
}
