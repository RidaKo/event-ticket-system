package com.paradise.event_ticket_system.notification.confirmation.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PurchaseConfirmationQueuedListener {

	private final PurchaseConfirmationDispatchService dispatchService;

	public PurchaseConfirmationQueuedListener(PurchaseConfirmationDispatchService dispatchService) {
		this.dispatchService = dispatchService;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onPurchaseConfirmationQueued(PurchaseConfirmationQueuedEvent event) {
		dispatchService.dispatch(event.deliveryId());
	}
}
