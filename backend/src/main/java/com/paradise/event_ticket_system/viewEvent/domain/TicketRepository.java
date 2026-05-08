package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}
