package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.model.PurchaseOrder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class TicketUrlBuilder {

    private final TicketProperties properties;

    public TicketUrlBuilder(TicketProperties properties) {
        this.properties = properties;
    }

    public String verifyUrl(String ticketCode, String orderToken) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(apiBase() + "/api/tickets/verify/" + ticketCode);
        if (StringUtils.hasText(orderToken)) {
            builder.queryParam("orderToken", orderToken);
        }
        return builder.build().toUriString();
    }

    public String qrImageUrl(String ticketCode, String orderToken) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(apiBase() + "/api/tickets/" + ticketCode + "/qr");
        if (StringUtils.hasText(orderToken)) {
            builder.queryParam("orderToken", orderToken);
        }
        return builder.build().toUriString();
    }

    public String orderConfirmationUrl(PurchaseOrder order) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromUriString(frontendBase() + "/checkout/" + order.getOrderNumber() + "/confirmation");
        if (StringUtils.hasText(order.getOrderToken())) {
            builder.queryParam("token", order.getOrderToken());
        }
        return builder.build().toUriString();
    }

    private String apiBase() {
        return trimTrailingSlash(properties.publicApiBaseUrl());
    }

    private String frontendBase() {
        return trimTrailingSlash(properties.publicFrontendBaseUrl());
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
