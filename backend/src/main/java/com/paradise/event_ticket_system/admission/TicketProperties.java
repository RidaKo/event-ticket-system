package com.paradise.event_ticket_system.admission;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.tickets")
public record TicketProperties(
    String publicApiBaseUrl,
    String publicFrontendBaseUrl
) {
}
