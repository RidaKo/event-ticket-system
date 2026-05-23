package com.paradise.event_ticket_system.notification.confirmation.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.paradise.event_ticket_system.admission.TicketRepository;
import com.paradise.event_ticket_system.admission.TicketUrlBuilder;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.OrderItem;
import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.model.PurchaseConfirmationTicketLine;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.Ticket;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PurchaseConfirmationDeliveryFactory {

	private final TicketRepository ticketRepository;
	private final TicketUrlBuilder ticketUrlBuilder;

	public PurchaseConfirmationDeliveryFactory(TicketRepository ticketRepository, TicketUrlBuilder ticketUrlBuilder) {
		this.ticketRepository = ticketRepository;
		this.ticketUrlBuilder = ticketUrlBuilder;
	}

	public PurchaseConfirmationDelivery createPending(PurchaseOrder order) {
		if (order.getId() == null) {
			throw new IllegalArgumentException("Order must be persisted before creating a confirmation delivery");
		}
		if (order.getItems().isEmpty()) {
			throw new IllegalArgumentException("Order %s does not contain any tickets".formatted(order.getId()));
		}

		Event event = order.getEvent();
		List<PurchaseConfirmationTicketLine> ticketLines = summarizeTicketLines(order.getItems());

		String orderAccessUrl = ticketUrlBuilder.orderConfirmationUrl(order);
		String qrCodeImageUrl = ticketRepository.findByPurchaseOrderIdOrderByIdAsc(order.getId()).stream()
			.map(Ticket::getQrCodeUrl)
			.filter(StringUtils::hasText)
			.findFirst()
			.orElse(null);

		return PurchaseConfirmationDelivery.pending(
			order.getId(),
			order.getOrderNumber(),
			resolveRecipientEmail(order),
			event.getTitle(),
			OffsetDateTime.ofInstant(event.getStartDatetime(), ZoneId.of(event.getTimezone())),
			resolveEventLocation(event),
			totalQuantity(order.getItems()),
			ticketLines,
			orderAccessUrl,
			qrCodeImageUrl
		);
	}

	private List<PurchaseConfirmationTicketLine> summarizeTicketLines(List<OrderItem> items) {
		Map<String, Integer> quantitiesByType = items.stream()
			.collect(Collectors.groupingBy(
				this::ticketName,
				LinkedHashMap::new,
				Collectors.summingInt(OrderItem::getQuantity)
			));

		return quantitiesByType.entrySet().stream()
			.map(entry -> new PurchaseConfirmationTicketLine(entry.getKey(), entry.getValue()))
			.toList();
	}

	private int totalQuantity(List<OrderItem> items) {
		return items.stream()
			.mapToInt(OrderItem::getQuantity)
			.sum();
	}

	private String resolveRecipientEmail(PurchaseOrder order) {
		if (StringUtils.hasText(order.getGuestEmail())) {
			return order.getGuestEmail();
		}

		throw new IllegalStateException("Order %s does not have a single resolved recipient email".formatted(order.getId()));
	}

	private String ticketName(OrderItem item) {
		if (StringUtils.hasText(item.getTicketName())) {
			return item.getTicketName();
		}

		return item.getTicketType().getName();
	}

	private String resolveEventLocation(Event event) {
		return "%s, %s, %s, %s".formatted(
			event.getVenue().getName(),
			event.getVenue().getAddressLine1(),
			event.getVenue().getCity(),
			event.getVenue().getCountry()
		);
	}
}
