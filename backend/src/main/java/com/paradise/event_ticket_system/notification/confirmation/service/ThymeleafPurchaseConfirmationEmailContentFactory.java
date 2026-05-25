package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.admission.TicketQrCodeGenerator;
import com.paradise.event_ticket_system.admission.TicketRepository;
import com.paradise.event_ticket_system.admission.TicketUrlBuilder;
import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.model.PurchaseConfirmationTicketLine;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.order.PurchaseOrderRepository;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class ThymeleafPurchaseConfirmationEmailContentFactory {

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy 'at' HH:mm XXX", Locale.ENGLISH);

	private final TemplateEngine templateEngine;
	private final TicketRepository ticketRepository;
	private final PurchaseOrderRepository purchaseOrderRepository;
	private final TicketQrCodeGenerator qrCodeGenerator;
	private final TicketUrlBuilder ticketUrlBuilder;

	public ThymeleafPurchaseConfirmationEmailContentFactory(
		TemplateEngine templateEngine,
		TicketRepository ticketRepository,
		PurchaseOrderRepository purchaseOrderRepository,
		TicketQrCodeGenerator qrCodeGenerator,
		TicketUrlBuilder ticketUrlBuilder
	) {
		this.templateEngine = templateEngine;
		this.ticketRepository = ticketRepository;
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.qrCodeGenerator = qrCodeGenerator;
		this.ticketUrlBuilder = ticketUrlBuilder;
	}

	public PurchaseConfirmationEmailMessage createMessage(PurchaseConfirmationDelivery delivery) {
		PurchaseOrder order = purchaseOrderRepository.findById(delivery.getOrderId())
			.orElseThrow(() -> new IllegalStateException("Order %d not found for delivery".formatted(delivery.getOrderId())));

		List<Ticket> tickets = ticketRepository.findByPurchaseOrderIdWithDetails(delivery.getOrderId());
		List<EmailInlineImage> inlineImages = new ArrayList<>();
		List<PurchaseConfirmationTicketQrView> ticketQrs = new ArrayList<>();

		for (Ticket ticket : tickets) {
			String contentId = "ticket-" + ticket.getId();
			String verifyUrl = ticketUrlBuilder.verifyUrl(ticket.getTicketCode(), order.getOrderToken());
			byte[] png = qrCodeGenerator.generatePng(verifyUrl);
			inlineImages.add(new EmailInlineImage(contentId, png));
			ticketQrs.add(new PurchaseConfirmationTicketQrView(
				ticket.getTicketType().getName(),
				ticket.getTicketCode(),
				contentId
			));
		}

		Context context = createContext(delivery, ticketQrs);
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
			textBody,
			inlineImages
		);
	}

	private Context createContext(PurchaseConfirmationDelivery delivery, List<PurchaseConfirmationTicketQrView> ticketQrs) {
		Map<String, Object> variables = new LinkedHashMap<>();
		variables.put("eventTitle", delivery.getEventTitle());
		variables.put("eventDateTime", DATE_TIME_FORMATTER.format(delivery.getEventDateTime()));
		variables.put("eventLocation", delivery.getEventLocation());
		variables.put("ticketLines", delivery.getTicketLines());
		variables.put("formattedTicketLines", formatTicketLines(delivery.getTicketLines()));
		variables.put("totalQuantity", delivery.getTotalQuantity());
		variables.put("orderReference", delivery.getOrderReference());
		variables.put("orderAccessUrl", delivery.getOrderAccessUrl());
		variables.put("ticketQrs", ticketQrs);
		variables.put("ticketAccessSummary", resolveTicketAccessSummary(delivery, ticketQrs));
		variables.put("hasOrderAccessUrl", StringUtils.hasText(delivery.getOrderAccessUrl()));
		variables.put("hasTicketQrs", !ticketQrs.isEmpty());
		variables.put(
			"hasAccessDetails",
			StringUtils.hasText(delivery.getOrderAccessUrl()) || !ticketQrs.isEmpty()
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

	private String resolveTicketAccessSummary(PurchaseConfirmationDelivery delivery,
		List<PurchaseConfirmationTicketQrView> ticketQrs) {
		if (StringUtils.hasText(delivery.getOrderAccessUrl())) {
			return delivery.getOrderAccessUrl();
		}
		if (!ticketQrs.isEmpty()) {
			return "%d QR code(s) are attached in the HTML version of this email.".formatted(ticketQrs.size());
		}

		return "Open the order confirmation screen in Event Ticket System using order reference %s."
			.formatted(delivery.getOrderReference());
	}
}
