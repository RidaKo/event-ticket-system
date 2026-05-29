package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.admission.TicketQrCodeGenerator;
import com.paradise.event_ticket_system.admission.TicketRepository;
import com.paradise.event_ticket_system.admission.TicketUrlBuilder;
import com.paradise.event_ticket_system.model.EmailDeliveryStatus;
import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.model.PurchaseConfirmationTicketLine;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDeliveryRepository;
import com.paradise.event_ticket_system.order.PurchaseOrderRepository;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Component
public class ThymeleafPurchaseConfirmationEmailContentFactory {

	private static final DateTimeFormatter DATE_TIME_FORMATTER =
		DateTimeFormatter.ofPattern("EEE, dd MMM yyyy 'at' HH:mm XXX", Locale.ENGLISH);

	private final TemplateEngine templateEngine;
	private final PurchaseConfirmationDeliveryRepository deliveryRepository;
	private final TicketRepository ticketRepository;
	private final PurchaseOrderRepository purchaseOrderRepository;
	private final TicketQrCodeGenerator qrCodeGenerator;
	private final TicketUrlBuilder ticketUrlBuilder;

	public ThymeleafPurchaseConfirmationEmailContentFactory(
		TemplateEngine templateEngine,
		PurchaseConfirmationDeliveryRepository deliveryRepository,
		TicketRepository ticketRepository,
		PurchaseOrderRepository purchaseOrderRepository,
		TicketQrCodeGenerator qrCodeGenerator,
		TicketUrlBuilder ticketUrlBuilder
	) {
		this.templateEngine = templateEngine;
		this.deliveryRepository = deliveryRepository;
		this.ticketRepository = ticketRepository;
		this.purchaseOrderRepository = purchaseOrderRepository;
		this.qrCodeGenerator = qrCodeGenerator;
		this.ticketUrlBuilder = ticketUrlBuilder;
	}

	@Transactional(readOnly = true)
	public Optional<PurchaseConfirmationEmailData> loadPendingData(Long deliveryId) {
		PurchaseConfirmationDelivery delivery = purchaseConfirmationDelivery(deliveryId);
		if (delivery.getStatus() != EmailDeliveryStatus.PENDING) {
			return Optional.empty();
		}

		PurchaseOrder order = purchaseOrderRepository.findById(delivery.getOrderId())
			.orElseThrow(() -> new IllegalStateException("Order %d not found for delivery".formatted(delivery.getOrderId())));

		List<Ticket> tickets = ticketRepository.findByPurchaseOrderIdWithDetails(delivery.getOrderId());
		List<PurchaseConfirmationEmailData.TicketEmailData> ticketData = tickets.stream()
			.map(ticket -> {
				String contentId = "ticket-" + ticket.getId();
				String verifyUrl = ticketUrlBuilder.verifyUrl(ticket.getTicketCode(), order.getOrderToken());
				return new PurchaseConfirmationEmailData.TicketEmailData(
					ticket.getTicketType().getName(),
					ticket.getTicketCode(),
					contentId,
					verifyUrl
				);
			})
			.toList();

		return Optional.of(new PurchaseConfirmationEmailData(
			delivery.getId(),
			delivery.getOrderId(),
			delivery.getOrderReference(),
			delivery.getAttendeeEmail(),
			delivery.getEventTitle(),
			delivery.getEventDateTime(),
			delivery.getEventLocation(),
			delivery.getTotalQuantity(),
			delivery.getTicketLines(),
			delivery.getOrderAccessUrl(),
			ticketData
		));
	}

	public PurchaseConfirmationEmailMessage createMessage(PurchaseConfirmationEmailData data) {
		List<EmailInlineImage> inlineImages = new ArrayList<>();
		List<PurchaseConfirmationTicketQrView> ticketQrs = new ArrayList<>();

		for (PurchaseConfirmationEmailData.TicketEmailData ticket : data.tickets()) {
			byte[] png = qrCodeGenerator.generatePng(ticket.verifyUrl());
			inlineImages.add(new EmailInlineImage(ticket.contentId(), png));
			ticketQrs.add(new PurchaseConfirmationTicketQrView(
				ticket.ticketType(),
				ticket.ticketCode(),
				ticket.contentId()
			));
		}

		Context context = createContext(data, ticketQrs);
		String htmlBody = templateEngine.process("email/purchase-confirmation", context);
		String textBody = templateEngine.process("email/purchase-confirmation-text", context);

		String subject = "Your tickets for %s (%s)".formatted(
			data.eventTitle(),
			data.orderReference()
		);

		return new PurchaseConfirmationEmailMessage(
			data.attendeeEmail(),
			subject,
			htmlBody,
			textBody,
			inlineImages
		);
	}

	private PurchaseConfirmationDelivery purchaseConfirmationDelivery(Long deliveryId) {
		return deliveryRepository.findById(deliveryId)
			.orElseThrow(() -> new IllegalArgumentException("Unknown delivery id: " + deliveryId));
	}

	private Context createContext(PurchaseConfirmationEmailData data, List<PurchaseConfirmationTicketQrView> ticketQrs) {
		Map<String, Object> variables = new LinkedHashMap<>();
		variables.put("eventTitle", data.eventTitle());
		variables.put("eventDateTime", DATE_TIME_FORMATTER.format(data.eventDateTime()));
		variables.put("eventLocation", data.eventLocation());
		variables.put("ticketLines", data.ticketLines());
		variables.put("formattedTicketLines", formatTicketLines(data.ticketLines()));
		variables.put("totalQuantity", data.totalQuantity());
		variables.put("orderReference", data.orderReference());
		variables.put("orderAccessUrl", data.orderAccessUrl());
		variables.put("ticketQrs", ticketQrs);
		variables.put("ticketAccessSummary", resolveTicketAccessSummary(data, ticketQrs));
		variables.put("hasOrderAccessUrl", StringUtils.hasText(data.orderAccessUrl()));
		variables.put("hasTicketQrs", !ticketQrs.isEmpty());
		variables.put(
			"hasAccessDetails",
			StringUtils.hasText(data.orderAccessUrl()) || !ticketQrs.isEmpty()
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

	private String resolveTicketAccessSummary(PurchaseConfirmationEmailData data,
		List<PurchaseConfirmationTicketQrView> ticketQrs) {
		if (StringUtils.hasText(data.orderAccessUrl())) {
			return data.orderAccessUrl();
		}
		if (!ticketQrs.isEmpty()) {
			return "%d QR code(s) are attached in the HTML version of this email.".formatted(ticketQrs.size());
		}

		return "Open the order confirmation screen in Event Ticket System using order reference %s."
			.formatted(data.orderReference());
	}
}
