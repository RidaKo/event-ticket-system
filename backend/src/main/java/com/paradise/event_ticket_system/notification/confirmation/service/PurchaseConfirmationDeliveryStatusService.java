package com.paradise.event_ticket_system.notification.confirmation.service;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.model.EmailDeliveryStatus;
import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import com.paradise.event_ticket_system.notification.confirmation.domain.PurchaseConfirmationDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuditedBusinessAction
public class PurchaseConfirmationDeliveryStatusService {

    private final PurchaseConfirmationDeliveryRepository deliveryRepository;

    public PurchaseConfirmationDeliveryStatusService(PurchaseConfirmationDeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public void markSent(Long deliveryId) {
        PurchaseConfirmationDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown delivery id: " + deliveryId));
        if (delivery.getStatus() == EmailDeliveryStatus.PENDING) {
            delivery.markSent();
        }
    }

    @Transactional
    public void markFailed(Long deliveryId, String failureReason) {
        PurchaseConfirmationDelivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown delivery id: " + deliveryId));
        if (delivery.getStatus() == EmailDeliveryStatus.PENDING) {
            delivery.markFailed(failureReason);
        }
    }
}
