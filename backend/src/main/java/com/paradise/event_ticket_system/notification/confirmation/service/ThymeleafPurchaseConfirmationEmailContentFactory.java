package com.paradise.event_ticket_system.notification.confirmation.service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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
		Context context = createContext(delivery);
		String htmlBody = templateEngine.process("email/purchase-confirmation", context);
		String textBody = templateEngine.process("email/purchase-confirmation-text", context);

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

	private Context createContext(PurchaseConfirmationDelivery delivery) {
		Map<String, Object> variables = new LinkedHashMap<>();
		variables.put("eventTitle", delivery.getEventTitle());
		variables.put("eventDateTime", DATE_TIME_FORMATTER.format(delivery.getEventDateTime()));
		variables.put("eventLocation", delivery.getEventLocation());
		variables.put("ticketLines", delivery.getTicketLines());
		variables.put("formattedTicketLines", formatTicketLines(delivery.getTicketLines()));
		variables.put("totalQuantity", delivery.getTotalQuantity());
		variables.put("orderReference", delivery.getOrderReference());
		variables.put("orderAccessUrl", delivery.getOrderAccessUrl());
		variables.put("qrCodeImageUrl", delivery.getQrCodeImageUrl());
		variables.put("ticketAccessSummary", resolveTicketAccessSummary(delivery));
		variables.put("hasOrderAccessUrl", StringUtils.hasText(delivery.getOrderAccessUrl()));
		variables.put("hasQrCodeImageUrl", StringUtils.hasText(delivery.getQrCodeImageUrl()));
		variables.put(
			"hasAccessDetails",
			StringUtils.hasText(delivery.getOrderAccessUrl()) || StringUtils.hasText(delivery.getQrCodeImageUrl())
		);

		Context context = new Context(Locale.ENGLISH);
		context.setVariables(variables);
		return context;
	}

	private String formatTicketLines(java.util.List<PurchaseConfirmationTicketLine> ticketLines) {
		return ticketLines.stream()
			.map(ticketLine -> "- %s x %d".formatted(ticketLine.ticketType(), ticketLine.quantity()))
			.collect(Collectors.joining("\n"));
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
