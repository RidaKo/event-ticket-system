package com.paradise.event_ticket_system.notification.confirmation.service;

public record PurchaseConfirmationTicketQrView(
    String ticketTypeName,
    String ticketCode,
    String contentId
) {
}
