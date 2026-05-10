package com.paradise.event_ticket_system.checkout;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TicketItemRequest(
        @NotNull Integer ticketTypeId,
        @NotNull @Min(1) Integer quantity
) {
}
