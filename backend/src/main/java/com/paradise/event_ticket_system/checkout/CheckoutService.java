package com.paradise.event_ticket_system.checkout;

import com.paradise.event_ticket_system.discount.DiscountCode;
import com.paradise.event_ticket_system.discount.DiscountRepository;
import com.paradise.event_ticket_system.discount.DiscountType;
import com.paradise.event_ticket_system.event.CheckoutCatalogRules;
import com.paradise.event_ticket_system.event.EventRepository;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.order.OrderItem;
import com.paradise.event_ticket_system.order.OrderStatus;
import com.paradise.event_ticket_system.order.PurchaseOrder;
import com.paradise.event_ticket_system.order.PurchaseOrderRepository;
import com.paradise.event_ticket_system.payment.Payment;
import com.paradise.event_ticket_system.payment.PaymentMethodType;
import com.paradise.event_ticket_system.payment.PaymentRepository;
import com.paradise.event_ticket_system.payment.PaymentResult;
import com.paradise.event_ticket_system.payment.PaymentService;
import com.paradise.event_ticket_system.payment.PaymentStatus;
import com.paradise.event_ticket_system.ticket.TicketTypeRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CheckoutService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final DiscountRepository discountRepository;
    private final PurchaseOrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public CheckoutService(
            EventRepository eventRepository,
            TicketTypeRepository ticketTypeRepository,
            DiscountRepository discountRepository,
            PurchaseOrderRepository orderRepository,
            PaymentRepository paymentRepository,
            PaymentService paymentService
    ) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.discountRepository = discountRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @Transactional(readOnly = true)
    public OrderSummaryResponse quote(CheckoutQuoteRequest request) {
        Event event = loadEventForCheckout(request.eventId());
        return buildSummary(event, mergeItems(request.items()), request.discountCode());
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Event event = loadEventForCheckout(request.eventId());
        Map<Integer, Integer> requestedItems = mergeItems(request.items());
        OrderSummaryResponse summary = buildSummary(event, requestedItems, request.discountCode());

        PurchaseOrder order = new PurchaseOrder();
        order.setOrderNumber("ORD-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setEvent(event);
        order.setGuestName(blankToNull(request.guestName()));
        order.setGuestEmail(request.guestEmail());
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(summary.subtotal());
        order.setDiscountAmount(summary.discountAmount());
        order.setTotalAmount(summary.total());

        if (hasText(request.discountCode())) {
            DiscountCode discountCode = loadValidDiscount(event.getId(), request.discountCode());
            order.setDiscountCode(discountCode);
        }

        Map<Integer, TicketType> ticketTypes = loadTicketTypes(requestedItems.keySet()).stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));
        summary.items().forEach(line -> {
            TicketType ticketType = ticketTypes.get(line.ticketTypeId());
            OrderItem item = new OrderItem();
            item.setTicketType(ticketType);
            item.setTicketName(line.name());
            item.setUnitPrice(line.unitPrice());
            item.setQuantity(line.quantity());
            item.setLineTotal(line.lineTotal());
            order.addItem(item);
        });

        return toOrderResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::toOrderResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    @Transactional
    public OrderResponse applyDiscount(String orderNumber, String discountCode) {
        PurchaseOrder order = loadOrderForUpdate(orderNumber);
        requirePending(order);

        Map<Integer, Integer> requestedItems = order.getItems().stream()
                .collect(Collectors.toMap(item -> item.getTicketType().getId(), OrderItem::getQuantity));
        OrderSummaryResponse summary = buildSummary(order.getEvent(), requestedItems, discountCode);
        order.setSubtotal(summary.subtotal());
        order.setDiscountAmount(summary.discountAmount());
        order.setTotalAmount(summary.total());
        order.setDiscountCode(hasText(discountCode) ? loadValidDiscount(order.getEvent().getId(), discountCode) : null);

        return toOrderResponse(order);
    }

    @Transactional
    public PaymentResponse submitPayment(String orderNumber, PaymentRequest request) {
        PurchaseOrder order = loadOrderForUpdate(orderNumber);
        requirePending(order);

        if (order.getDiscountCode() != null) {
            validateDiscount(order.getDiscountCode());
        }

        List<TicketType> lockedTicketTypes = ticketTypeRepository.findAllByIdForUpdate(order.getItems().stream()
                .map(item -> item.getTicketType().getId())
                .toList());
        Map<Integer, TicketType> ticketTypeById = lockedTicketTypes.stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));

        for (OrderItem item : order.getItems()) {
            TicketType ticketType = ticketTypeById.get(item.getTicketType().getId());
            validateTicketCanBePurchased(order.getEvent(), ticketType, item.getQuantity());
        }

        PaymentResult result = paymentService.charge(order.getTotalAmount(), request.methodType(), request.cardNumber());
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethodType(request.methodType());
        payment.setProviderReference(result.providerReference());
        payment.setCardLast4(result.cardLast4());

        if (!result.successful()) {
            payment.setStatus(PaymentStatus.FAILED);
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setPayment(payment);
            paymentRepository.save(payment);
            return new PaymentResponse(order.getOrderNumber(), order.getStatus(), payment.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            TicketType ticketType = ticketTypeById.get(item.getTicketType().getId());
            int quantitySold = ticketType.getQuantitySold() == null ? 0 : ticketType.getQuantitySold();
            ticketType.setQuantitySold(quantitySold + item.getQuantity());
        }

        if (order.getDiscountCode() != null) {
            order.getDiscountCode().setUsedCount(order.getDiscountCode().getUsedCount() + 1);
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        order.setPayment(payment);
        paymentRepository.save(payment);

        return new PaymentResponse(order.getOrderNumber(), order.getStatus(), payment.getStatus());
    }

    @Transactional(readOnly = true)
    public ConfirmationResponse confirmation(String orderNumber) {
        PurchaseOrder order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not confirmed");
        }
        Event event = order.getEvent();
        Venue venue = event.getVenue();
        Payment payment = order.getPayment();
        return new ConfirmationResponse(
                order.getOrderNumber(),
                order.getStatus(),
                order.getGuestName(),
                order.getGuestEmail(),
                order.getConfirmedAt(),
                new EventSummaryResponse(
                        event.getId(),
                        event.getTitle(),
                        event.getDescription(),
                        event.getStartDatetime(),
                        event.getEndDatetime(),
                        venue.getName(),
                        venue.getAddressLine1(),
                        venue.getCity(),
                        venue.getCountry()
                ),
                toSummary(order),
                payment == null ? null : payment.getMethodType(),
                payment == null ? null : payment.getCardLast4()
        );
    }

    private Event loadEventForCheckout(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        if (!CheckoutCatalogRules.isEventSalesEnabled(event)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ticket sales are disabled for this event");
        }
        if (event.getStartDatetime() != null && !event.getStartDatetime().isAfter(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Event sales are closed");
        }
        return event;
    }

    private OrderSummaryResponse buildSummary(Event event, Map<Integer, Integer> requestedItems, String discountCode) {
        if (requestedItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Select at least one ticket");
        }

        Map<Integer, TicketType> ticketTypes = loadTicketTypes(requestedItems.keySet()).stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));
        if (ticketTypes.size() != requestedItems.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown ticket type selected");
        }

        List<TicketLineResponse> lines = requestedItems.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    TicketType ticketType = ticketTypes.get(entry.getKey());
                    validateTicketCanBePurchased(event, ticketType, entry.getValue());
                    BigDecimal quantity = BigDecimal.valueOf(entry.getValue());
                    BigDecimal lineTotal = money(ticketType.getPrice().multiply(quantity));
                    return new TicketLineResponse(
                            ticketType.getId(),
                            ticketType.getName(),
                            money(ticketType.getPrice()),
                            entry.getValue(),
                            lineTotal
                    );
                })
                .toList();

        BigDecimal subtotal = money(lines.stream()
                .map(TicketLineResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        DiscountCode discount = hasText(discountCode) ? loadValidDiscount(event.getId(), discountCode) : null;
        BigDecimal discountAmount = discount == null ? BigDecimal.ZERO : calculateDiscount(discount, subtotal);

        return new OrderSummaryResponse(
                lines,
                subtotal,
                discount == null ? null : discount.getCode(),
                money(discountAmount),
                money(subtotal.subtract(discountAmount).max(BigDecimal.ZERO))
        );
    }

    private List<TicketType> loadTicketTypes(Collection<Integer> ticketTypeIds) {
        return ticketTypeRepository.findAllById(ticketTypeIds).stream()
                .sorted(Comparator.comparing(TicketType::getId))
                .toList();
    }

    private void validateTicketCanBePurchased(Event event, TicketType ticketType, int quantity) {
        if (ticketType == null || !ticketType.getEvent().getId().equals(event.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket type does not belong to this event");
        }
        if (!CheckoutCatalogRules.isTicketSalesEnabled(ticketType)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ticketType.getName() + " is not available");
        }
        if (quantity < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket quantity must be at least 1");
        }
        if (quantity > CheckoutCatalogRules.availableQuantity(ticketType)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough tickets available for " + ticketType.getName());
        }
    }

    private DiscountCode loadValidDiscount(Integer eventId, String discountCode) {
        DiscountCode discount = discountRepository.findByCodeIgnoreCaseAndEventId(discountCode.trim(), eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount code is invalid"));
        validateDiscount(discount);
        return discount;
    }

    private void validateDiscount(DiscountCode discount) {
        if (!discount.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount code is inactive");
        }
        if (discount.getExpiresAt() != null && discount.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Discount code has expired");
        }
        if (!discount.hasRedemptionsLeft()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Discount code has reached its usage limit");
        }
    }

    private BigDecimal calculateDiscount(DiscountCode discount, BigDecimal subtotal) {
        BigDecimal amount = switch (discount.getType()) {
            case PERCENT -> subtotal.multiply(discount.getValue()).divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
            case FIXED -> discount.getValue();
        };
        return money(amount.min(subtotal));
    }

    private Map<Integer, Integer> mergeItems(List<TicketItemRequest> items) {
        Map<Integer, Integer> merged = new HashMap<>();
        for (TicketItemRequest item : items) {
            if (item.quantity() == null || item.quantity() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket quantity must be at least 1");
            }
            merged.merge(item.ticketTypeId(), item.quantity(), Integer::sum);
        }
        return merged;
    }

    private PurchaseOrder loadOrderForUpdate(String orderNumber) {
        return orderRepository.findByOrderNumberForUpdate(orderNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private void requirePending(PurchaseOrder order) {
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Order is not pending payment");
        }
    }

    private OrderResponse toOrderResponse(PurchaseOrder order) {
        return new OrderResponse(
                order.getOrderNumber(),
                order.getStatus(),
                order.getEvent().getId(),
                order.getGuestName(),
                order.getGuestEmail(),
                toSummary(order)
        );
    }

    private OrderSummaryResponse toSummary(PurchaseOrder order) {
        return new OrderSummaryResponse(
                order.getItems().stream()
                        .sorted(Comparator.comparing(OrderItem::getId))
                        .map(item -> new TicketLineResponse(
                                item.getTicketType().getId(),
                                item.getTicketName(),
                                money(item.getUnitPrice()),
                                item.getQuantity(),
                                money(item.getLineTotal())
                        ))
                        .toList(),
                money(order.getSubtotal()),
                order.getDiscountCode() == null ? null : order.getDiscountCode().getCode(),
                money(order.getDiscountAmount()),
                money(order.getTotalAmount())
        );
    }

    private BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String blankToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
