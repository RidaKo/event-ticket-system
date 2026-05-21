package com.paradise.event_ticket_system.notification.confirmation.api;

import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationDispatchResult;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal/notifications/purchase-confirmations")
public class PurchaseConfirmationController {

	private final PurchaseConfirmationService purchaseConfirmationService;

	public PurchaseConfirmationController(PurchaseConfirmationService purchaseConfirmationService) {
		this.purchaseConfirmationService = purchaseConfirmationService;
	}

	@PostMapping
	public ResponseEntity<PurchaseConfirmationResponse> queuePurchaseConfirmation(
		@Valid @RequestBody PurchaseConfirmationRequest request
	) {
		PurchaseConfirmationDispatchResult result = purchaseConfirmationService.handle(request);
		PurchaseConfirmationResponse response = new PurchaseConfirmationResponse(
			result.deliveryId(),
			result.orderId(),
			result.orderReference(),
			result.outcome(),
			result.alreadyProcessed()
		);

		HttpStatus status = result.outcome() == PurchaseConfirmationOutcome.QUEUED
			? HttpStatus.ACCEPTED
			: HttpStatus.OK;

		return ResponseEntity.status(status).body(response);
	}
}
