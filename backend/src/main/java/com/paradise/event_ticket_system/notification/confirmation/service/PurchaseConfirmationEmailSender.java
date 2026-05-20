package com.paradise.event_ticket_system.notification.confirmation.service;

public interface PurchaseConfirmationEmailSender {

	void send(PurchaseConfirmationEmailMessage message);
}
