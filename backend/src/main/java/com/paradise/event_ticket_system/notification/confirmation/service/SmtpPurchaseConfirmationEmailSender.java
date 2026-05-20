package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.notification.confirmation.config.EmailDeliveryProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "event-ticket.email", name = "sender", havingValue = "smtp")
public class SmtpPurchaseConfirmationEmailSender implements PurchaseConfirmationEmailSender {

	private final JavaMailSender mailSender;
	private final EmailDeliveryProperties emailDeliveryProperties;

	public SmtpPurchaseConfirmationEmailSender(
		JavaMailSender mailSender,
		EmailDeliveryProperties emailDeliveryProperties
	) {
		this.mailSender = mailSender;
		this.emailDeliveryProperties = emailDeliveryProperties;
	}

	@Override
	public void send(PurchaseConfirmationEmailMessage message) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();

		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			helper.setFrom(emailDeliveryProperties.getFromAddress());
			helper.setTo(message.to());
			helper.setSubject(message.subject());
			helper.setText(message.textBody(), message.htmlBody());
			mailSender.send(mimeMessage);
		}
		catch (MessagingException ex) {
			throw new IllegalStateException("Failed to compose purchase confirmation email", ex);
		}
	}
}
