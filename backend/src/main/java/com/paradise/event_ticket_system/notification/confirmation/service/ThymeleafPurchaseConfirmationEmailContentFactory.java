package com.paradise.event_ticket_system.notification.confirmation.service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDelivery;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class ThymeleafPurchaseConfirmationEmailContentFactory {

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy 'at' HH:mm XXX", Locale.ENGLISH);

	private final TemplateEngine templateEngine;

	public ThymeleafPurchaseConfirmationEmailContentFactory(TemplateEngine templateEngine) {
		this.templateEngine = templateEngine;
	}

	public PurchaseConfirmationEmailMessage createMessage(PurchaseConfirmationDelivery delivery) {
		Context context = new Context(Locale.ENGLISH);
		context.setVariable("eventTitle", delivery.getEventTitle());
		context.setVariable("eventDateTime", DATE_TIME_FORMATTER.format(delivery.getEventDateTime()));
		context.setVariable("eventLocation", delivery.getEventLocation());
		context.setVariable("ticketType", delivery.getTicketType());
		context.setVariable("quantity", delivery.getQuantity());
		context.setVariable("orderReference", delivery.getOrderReference());
		context.setVariable("ticketUrl", delivery.getTicketUrl());
		context.setVariable("qrCodeImageUrl", delivery.getQrCodeImageUrl());
		context.setVariable("hasTicketUrl", StringUtils.hasText(delivery.getTicketUrl()));
		context.setVariable("hasQrCodeImageUrl", StringUtils.hasText(delivery.getQrCodeImageUrl()));

		String htmlBody = templateEngine.process("email/purchase-confirmation", context);
		String textBody = """
			Hello,
			
			Your purchase is confirmed.
			
			Event: %s
			Date and time: %s
			Location: %s
			Ticket type: %s
			Quantity: %d
			Order reference: %s
			
			Ticket access: %s
			
			Thanks,
			Event Ticket System
			""".formatted(
			delivery.getEventTitle(),
			DATE_TIME_FORMATTER.format(delivery.getEventDateTime()),
			delivery.getEventLocation(),
			delivery.getTicketType(),
			delivery.getQuantity(),
			delivery.getOrderReference(),
			resolveTicketAccessSummary(delivery)
		);

		String subject = "Your tickets for %s (%s)".formatted(
			delivery.getEventTitle(),
			delivery.getOrderReference()
		);

		return new PurchaseConfirmationEmailMessage(
			delivery.getAttendeeEmail(),
			subject,
			htmlBody,
			textBody
		);
	}

	private String resolveTicketAccessSummary(PurchaseConfirmationDelivery delivery) {
		if (StringUtils.hasText(delivery.getTicketUrl())) {
			return delivery.getTicketUrl();
		}

		return "QR code included in the HTML version of this email.";
	}
}
