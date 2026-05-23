package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.model.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketAdmissionService {

    private final TicketRepository ticketRepository;

    public TicketAdmissionService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional(readOnly = true)
    public TicketVerifyResponse verify(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCodeWithDetails(ticketCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        return new TicketVerifyResponse(
            ticket.getTicketCode(),
            ticket.getStatus(),
            ticket.getTicketType().getName(),
            ticket.getEvent().getTitle(),
            ticket.getEvent().getStartDatetime(),
            ticket.getCheckedInAt() != null
        );
    }

    @Transactional(readOnly = true)
    public Ticket loadByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }
}
