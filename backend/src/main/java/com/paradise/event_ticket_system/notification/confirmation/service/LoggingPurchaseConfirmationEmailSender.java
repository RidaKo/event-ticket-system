package com.paradise.event_ticket_system.notification.confirmation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
	prefix = "event-ticket.email",
	name = "sender",
	havingValue = "log",
	matchIfMissing = true
)
public class LoggingPurchaseConfirmationEmailSender implements PurchaseConfirmationEmailSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingPurchaseConfirmationEmailSender.class);

	@Override
	public void send(PurchaseConfirmationEmailMessage message) {
		log.info(
			"Sending purchase confirmation email in log mode to {} with subject '{}' ({} inline QR image(s)).\n{}",
			message.to(),
			message.subject(),
			message.inlineImages().size(),
			message.textBody()
		);
	}
}
