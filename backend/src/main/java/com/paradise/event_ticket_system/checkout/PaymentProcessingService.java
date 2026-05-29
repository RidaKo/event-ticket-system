package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.payment.PaymentResult;
import com.paradise.event_ticket_system.payment.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AuditedBusinessAction
public class PaymentProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingService.class);

    private final PaymentService paymentService;
    private final PaymentFinalizationService finalizationService;

    public PaymentProcessingService(
            PaymentService paymentService,
            PaymentFinalizationService finalizationService
    ) {
        this.paymentService = paymentService;
        this.finalizationService = finalizationService;
    }

    public void process(PaymentRequestedEvent event) {
        PaymentResult result;
        try {
            result = paymentService.charge(event.amount(), event.methodType(), event.cardNumber());
        } catch (RuntimeException ex) {
            log.error("Payment provider failed for order {}", event.orderNumber(), ex);
            result = new PaymentResult(false, null, null);
        }
        finalizationService.complete(event.orderNumber(), result);
    }
}
