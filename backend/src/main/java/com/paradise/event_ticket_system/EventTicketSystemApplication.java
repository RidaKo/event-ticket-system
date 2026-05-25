package com.paradise.event_ticket_system;

import com.paradise.event_ticket_system.admission.TicketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(TicketProperties.class)
public class EventTicketSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventTicketSystemApplication.class, args);
	}

}
