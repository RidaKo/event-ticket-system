package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.discount.DiscountCode;
import com.paradise.event_ticket_system.discount.DiscountRepository;
import com.paradise.event_ticket_system.model.OrderItem;
import com.paradise.event_ticket_system.model.Payment;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.notification.confirmation.api.PurchaseConfirmationRequest;
import com.paradise.event_ticket_system.notification.confirmation.service.PurchaseConfirmationService;
import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.order.PurchaseOrderRepository;
import com.paradise.event_ticket_system.payment.PaymentResult;
import com.paradise.event_ticket_system.payment.PaymentStatus;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuditedBusinessAction
public class PaymentFinalizationService {

    private final PurchaseOrderRepository orderRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final DiscountRepository discountRepository;
    private final PurchaseConfirmationService purchaseConfirmationService;

    public PaymentFinalizationService(
            PurchaseOrderRepository orderRepository,
            TicketTypeRepository ticketTypeRepository,
            DiscountRepository discountRepository,
            PurchaseConfirmationService purchaseConfirmationService
    ) {
        this.orderRepository = orderRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.discountRepository = discountRepository;
        this.purchaseConfirmationService = purchaseConfirmationService;
    }

    @Transactional
    public void complete(String orderNumber, PaymentResult result) {
        PurchaseOrder order = orderRepository.findByOrderNumberForUpdate(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Unknown order number: " + orderNumber));
        Payment payment = order.getPayment();
        if (order.getStatus() != OrderStatus.PAYMENT_PROCESSING
                || payment == null
                || payment.getStatus() != PaymentStatus.PENDING) {
            return;
        }

        payment.setProviderReference(result.providerReference());
        payment.setCardLast4(result.cardLast4());

        if (!result.successful()) {
            releaseReservedResources(order);
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            return;
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        ensureOrderAccessToken(order);
        purchaseConfirmationService.handle(new PurchaseConfirmationRequest(order.getOrderNumber()));
    }

    private void releaseReservedResources(PurchaseOrder order) {
        List<Integer> ticketTypeIds = order.getItems().stream()
                .map(item -> item.getTicketType().getId())
                .toList();
        Map<Integer, TicketType> ticketTypeById = ticketTypeRepository.findAllByIdForUpdate(ticketTypeIds)
                .stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for (OrderItem item : order.getItems()) {
            TicketType ticketType = ticketTypeById.get(item.getTicketType().getId());
            if (ticketType != null) {
                int sold = ticketType.getQuantitySold() == null ? 0 : ticketType.getQuantitySold();
                ticketType.setQuantitySold(Math.max(0, sold - item.getQuantity()));
            }
        }

        if (order.getDiscountCode() != null) {
            DiscountCode discount = discountRepository.findByIdForUpdate(order.getDiscountCode().getId())
                    .orElse(null);
            if (discount != null && discount.getUsedCount() > 0) {
                discount.setUsedCount(discount.getUsedCount() - 1);
            }
        }
    }

    private void ensureOrderAccessToken(PurchaseOrder order) {
        if (order.getOrderToken() == null || order.getOrderToken().trim().isEmpty()) {
            order.setOrderToken(UUID.randomUUID().toString());
        }
    }
}
