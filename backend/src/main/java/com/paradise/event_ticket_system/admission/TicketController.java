package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.model.Ticket;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<byte[]> qrCode(
        @PathVariable String ticketCode,
        @RequestParam(value = "orderToken", required = false) String orderTokenParam,
        @RequestHeader(value = "X-Order-Token", required = false) String orderTokenHeader,
        Authentication auth
    ) {
        String orderToken = resolveOrderToken(orderTokenParam, orderTokenHeader);
        Ticket ticket = ticketAdmissionService.loadByCode(ticketCode, auth, orderToken);
        if (!TicketStatus.VALID.equals(ticket.getStatus()) && !TicketStatus.USED.equals(ticket.getStatus())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Ticket is not valid");
        }
        String orderTokenForQr = ticket.getPurchaseOrder().getOrderToken();
        byte[] png = qrCodeGenerator.generatePng(
            ticketUrlBuilder.verifyUrl(ticket.getTicketCode(), orderTokenForQr)
        );
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(png);
    }

    @GetMapping("/verify/{ticketCode}")
    public TicketVerifyResponse verify(
        @PathVariable String ticketCode,
        @RequestParam(value = "orderToken", required = false) String orderTokenParam,
        @RequestHeader(value = "X-Order-Token", required = false) String orderTokenHeader,
        Authentication auth
    ) {
        return ticketAdmissionService.verify(ticketCode, auth, resolveOrderToken(orderTokenParam, orderTokenHeader));
    }

    private static String resolveOrderToken(String orderTokenParam, String orderTokenHeader) {
        if (orderTokenParam != null && !orderTokenParam.isBlank()) {
            return orderTokenParam;
        }
        return orderTokenHeader;
    }
}
