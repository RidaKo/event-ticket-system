package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.TicketType;
import java.time.Instant;
import java.util.Set;

public final class CheckoutCatalogRules {

    private static final Set<String> CLOSED_EVENT_STATUSES = Set.of("DRAFT", "CANCELLED", "CANCELED", "ARCHIVED");

    private CheckoutCatalogRules() {
    }

    public static boolean isEventSalesEnabled(Event event) {
        String status = event.getStatus();
        return status != null && !CLOSED_EVENT_STATUSES.contains(status.toUpperCase());
    }

    public static boolean isTicketSalesEnabled(TicketType ticketType) {
        Instant now = Instant.now();
        return Boolean.TRUE.equals(ticketType.getIsActive())
                && (ticketType.getSaleStart() == null || !ticketType.getSaleStart().isAfter(now))
                && (ticketType.getSaleEnd() == null || !ticketType.getSaleEnd().isBefore(now));
    }

    public static int availableQuantity(TicketType ticketType) {
        int quantityTotal = ticketType.getQuantityTotal() == null ? 0 : ticketType.getQuantityTotal();
        int quantitySold = ticketType.getQuantitySold() == null ? 0 : ticketType.getQuantitySold();
        return Math.max(0, quantityTotal - quantitySold);
    }
}
