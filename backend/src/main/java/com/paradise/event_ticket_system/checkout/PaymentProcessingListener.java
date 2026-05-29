package com.paradise.event_ticket_system.checkout;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentProcessingListener {

    private final PaymentProcessingService paymentProcessingService;

    public PaymentProcessingListener(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    @Async("paymentTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRequested(PaymentRequestedEvent event) {
        paymentProcessingService.process(event);
    }
}
