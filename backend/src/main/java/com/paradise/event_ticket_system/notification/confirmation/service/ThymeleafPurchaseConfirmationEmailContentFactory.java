package com.paradise.event_ticket_system.notification.confirmation.service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.model.PurchaseConfirmationTicketLine;
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
		context.setVariable("ticketLines", delivery.getTicketLines());
		context.setVariable("totalQuantity", delivery.getTotalQuantity());
		context.setVariable("orderReference", delivery.getOrderReference());
		context.setVariable("orderAccessUrl", delivery.getOrderAccessUrl());
		context.setVariable("qrCodeImageUrl", delivery.getQrCodeImageUrl());
		context.setVariable("hasOrderAccessUrl", StringUtils.hasText(delivery.getOrderAccessUrl()));
		context.setVariable("hasQrCodeImageUrl", StringUtils.hasText(delivery.getQrCodeImageUrl()));
		context.setVariable(
			"hasAccessDetails",
			StringUtils.hasText(delivery.getOrderAccessUrl()) || StringUtils.hasText(delivery.getQrCodeImageUrl())
		);

		String htmlBody = templateEngine.process("email/purchase-confirmation", context);
		String textBody = """
			Hello,
			
			Your purchase is confirmed.
			
			Event: %s
			Date and time: %s
			Location: %s
			Tickets:
			%s
			Total quantity: %d
			Order reference: %s
			
			Ticket access: %s
			
			Thanks,
			Event Ticket System
			""".formatted(
			delivery.getEventTitle(),
			DATE_TIME_FORMATTER.format(delivery.getEventDateTime()),
			delivery.getEventLocation(),
			formatTicketLines(delivery.getTicketLines()),
			delivery.getTotalQuantity(),
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

	private String formatTicketLines(java.util.List<PurchaseConfirmationTicketLine> ticketLines) {
		return StringUtils.collectionToDelimitedString(
			ticketLines.stream()
				.map(ticketLine -> "- %s x %d".formatted(ticketLine.ticketType(), ticketLine.quantity()))
				.collect(Collectors.toList()),
			"\n"
		);
	}

	private String resolveTicketAccessSummary(PurchaseConfirmationDelivery delivery) {
		if (StringUtils.hasText(delivery.getOrderAccessUrl())) {
			return delivery.getOrderAccessUrl();
		}
		if (StringUtils.hasText(delivery.getQrCodeImageUrl())) {
			return "QR code included in the HTML version of this email.";
		}

		return "Open the order confirmation screen in Event Ticket System using order reference %s."
			.formatted(delivery.getOrderReference());
	}
}
