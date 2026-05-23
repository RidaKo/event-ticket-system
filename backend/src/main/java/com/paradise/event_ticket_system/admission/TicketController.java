package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.model.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketAdmissionService ticketAdmissionService;
    private final TicketQrCodeGenerator qrCodeGenerator;
    private final TicketUrlBuilder ticketUrlBuilder;

    public TicketController(
        TicketAdmissionService ticketAdmissionService,
        TicketQrCodeGenerator qrCodeGenerator,
        TicketUrlBuilder ticketUrlBuilder
    ) {
        this.ticketAdmissionService = ticketAdmissionService;
        this.qrCodeGenerator = qrCodeGenerator;
        this.ticketUrlBuilder = ticketUrlBuilder;
    }

    @GetMapping(value = "/{ticketCode}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@PathVariable String ticketCode) {
        Ticket ticket = ticketAdmissionService.loadByCode(ticketCode);
        if (!TicketStatus.VALID.equals(ticket.getStatus()) && !TicketStatus.USED.equals(ticket.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Ticket is not valid");
        }
        byte[] png = qrCodeGenerator.generatePng(ticketUrlBuilder.verifyUrl(ticket.getTicketCode()));
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    @GetMapping("/verify/{ticketCode}")
    public TicketVerifyResponse verify(@PathVariable String ticketCode) {
        return ticketAdmissionService.verify(ticketCode);
    }
}
