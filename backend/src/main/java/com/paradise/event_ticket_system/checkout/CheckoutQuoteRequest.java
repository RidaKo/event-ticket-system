package com.paradise.event_ticket_system.checkout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CheckoutQuoteRequest(
        @NotNull Integer eventId,
        @NotEmpty List<@Valid TicketItemRequest> items,
        String discountCode
) {
}
