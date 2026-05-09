package com.paradise.event_ticket_system.viewEvent.api.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketTypeRequest(
        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @PositiveOrZero
        BigDecimal price,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Total quantity is required")
        @Min(value = 1, message = "Total quantity must be at least 1")
        Integer quantityTotal,

        Instant saleStart,
        Instant saleEnd,

        @NotNull(message = "Max per order is required")
        @Min(value = 1, message = "Max per order must be at least 1")
        Integer maxPerOrder,

        Boolean isActive
) {}
