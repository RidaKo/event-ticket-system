package com.paradise.event_ticket_system.checkout;

public record IssuedTicketResponse(
    Integer id,
    String ticketCode,
    String ticketTypeName,
    String qrImageBase64,
    String status
) {
}
