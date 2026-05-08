package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.TicketType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {

    List<TicketType> findByEventId(Integer eventId);
}
