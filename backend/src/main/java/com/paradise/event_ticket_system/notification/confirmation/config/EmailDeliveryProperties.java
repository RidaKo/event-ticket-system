package com.paradise.event_ticket_system.notification.confirmation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "event-ticket.email")
public class EmailDeliveryProperties {

	private String sender = "log";
	private String fromAddress = "no-reply@event-ticket.local";

}
