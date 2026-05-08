package com.paradise.event_ticket_system.checkout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotNull Integer eventId,
        String guestName,
        @Email @NotEmpty String guestEmail,
        String discountCode,
        @NotEmpty List<@Valid TicketItemRequest> items
) {
}
