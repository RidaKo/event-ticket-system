package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.model.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketAdmissionService {

    private final TicketRepository ticketRepository;
    private final TicketAccessService ticketAccessService;

    public TicketAdmissionService(TicketRepository ticketRepository, TicketAccessService ticketAccessService) {
        this.ticketRepository = ticketRepository;
        this.ticketAccessService = ticketAccessService;
    }

    @Transactional(readOnly = true)
    public TicketVerifyResponse verify(String ticketCode, org.springframework.security.core.Authentication auth,
                                       String orderToken) {
        Ticket ticket = loadTicketWithOrder(ticketCode);
        ticketAccessService.verifyAccess(ticket, auth, orderToken);
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
    public Ticket loadByCode(String ticketCode, org.springframework.security.core.Authentication auth,
                            String orderToken) {
        Ticket ticket = loadTicketWithOrder(ticketCode);
        ticketAccessService.verifyAccess(ticket, auth, orderToken);
        return ticket;
    }

    private Ticket loadTicketWithOrder(String ticketCode) {
        return ticketRepository.findByTicketCodeWithOrder(ticketCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
    }
}
