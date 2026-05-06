package com.paradise.event_ticket_system.notification.confirmation.service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Order;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationTicketLine;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PurchaseConfirmationDeliveryFactory {

	public PurchaseConfirmationDelivery createPending(Order order, List<Ticket> tickets) {
		if (order.getId() == null) {
			throw new IllegalArgumentException("Order must be persisted before creating a confirmation delivery");
		}
		if (tickets.isEmpty()) {
			throw new IllegalArgumentException("Order %s does not contain any tickets".formatted(order.getId()));
		}

		Event event = resolveSingleEvent(order, tickets);
		List<PurchaseConfirmationTicketLine> ticketLines = summarizeTicketLines(tickets);

		return PurchaseConfirmationDelivery.pending(
			order.getId(),
			resolveOrderReference(order),
			resolveRecipientEmail(order, tickets),
			event.getTitle(),
			OffsetDateTime.ofInstant(event.getStartDatetime(), ZoneId.of(event.getTimezone())),
			resolveEventLocation(event),
			tickets.size(),
			ticketLines,
			null,
			resolveQrCodeImageUrl(tickets)
		);
	}

	private Event resolveSingleEvent(Order order, List<Ticket> tickets) {
		Map<Integer, Event> eventsById = tickets.stream()
			.map(Ticket::getEvent)
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(Event::getId, event -> event, (left, right) -> left, LinkedHashMap::new));

		if (eventsById.size() != 1) {
			throw new IllegalStateException(
				"Order %s spans %s events, but purchase confirmation emails currently support a single event per order"
					.formatted(order.getId(), eventsById.size())
			);
		}

		return eventsById.values().iterator().next();
	}

	private List<PurchaseConfirmationTicketLine> summarizeTicketLines(List<Ticket> tickets) {
		Map<String, Long> quantitiesByType = tickets.stream()
			.collect(Collectors.groupingBy(
				ticket -> ticket.getTicketType().getName(),
				LinkedHashMap::new,
				Collectors.counting()
			));

		return quantitiesByType.entrySet().stream()
			.map(entry -> new PurchaseConfirmationTicketLine(entry.getKey(), entry.getValue().intValue()))
			.toList();
	}

	private String resolveOrderReference(Order order) {
		if (StringUtils.hasText(order.getPaymentReference())) {
			return order.getPaymentReference();
		}

		return "ORD-%s".formatted(order.getId());
	}

	private String resolveRecipientEmail(Order order, List<Ticket> tickets) {
		if (order.getUser() != null && StringUtils.hasText(order.getUser().getEmail())) {
			return order.getUser().getEmail();
		}
		if (StringUtils.hasText(order.getGuestEmail())) {
			return order.getGuestEmail();
		}

		List<String> distinctOwnerEmails = tickets.stream()
			.map(Ticket::getOwnerEmail)
			.filter(StringUtils::hasText)
			.distinct()
			.toList();

		if (distinctOwnerEmails.size() == 1) {
			return distinctOwnerEmails.getFirst();
		}

		throw new IllegalStateException("Order %s does not have a single resolved recipient email".formatted(order.getId()));
	}

	private String resolveEventLocation(Event event) {
		return "%s, %s, %s, %s".formatted(
			event.getVenue().getName(),
			event.getVenue().getAddressLine1(),
			event.getVenue().getCity(),
			event.getVenue().getCountry()
		);
	}

	private String resolveQrCodeImageUrl(List<Ticket> tickets) {
		List<String> qrCodes = tickets.stream()
			.map(Ticket::getQrCodeUrl)
			.filter(StringUtils::hasText)
			.distinct()
			.toList();

		if (tickets.size() == 1 && qrCodes.size() == 1) {
			return qrCodes.getFirst();
		}

		return null;
	}
}
