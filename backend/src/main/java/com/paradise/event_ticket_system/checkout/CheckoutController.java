package com.paradise.event_ticket_system.checkout;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private static final String DEFAULT_ORDER_PAGE_SIZE = "10";

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/quote")
    public OrderSummaryResponse quote(@Valid @RequestBody CheckoutQuoteRequest request) {
        return checkoutService.quote(request);
    }

    @PostMapping("/orders/guest")
    public GuestOrderCreatedResponse createGuestOrder(@Valid @RequestBody GuestCreateOrderRequest request) {
        return checkoutService.createGuestOrder(request);
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAnyRole('USER','ORGANIZER','ADMIN')")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request, Authentication auth) {
        return checkoutService.createOrderForUser(request, auth.getName());
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('USER','ORGANIZER','ADMIN')")
    public UserOrdersResponse userOrders(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = DEFAULT_ORDER_PAGE_SIZE) int size
    ) {
        return checkoutService.getConfirmedOrdersForUser(auth.getName(), page, size);
    }

    @GetMapping("/orders/{orderNumber}")
    public OrderResponse getOrder(
            @PathVariable String orderNumber,
            Authentication auth,
            @RequestHeader(value = "X-Order-Token", required = false) String orderToken
    ) {
        checkoutService.verifyAccessTo(orderNumber, auth, orderToken);
        return checkoutService.getOrder(orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/discount")
    public OrderResponse applyDiscount(
            @PathVariable String orderNumber,
            @RequestBody ApplyDiscountRequest request,
            Authentication auth,
            @RequestHeader(value = "X-Order-Token", required = false) String orderToken
    ) {
        checkoutService.verifyAccessTo(orderNumber, auth, orderToken);
        return checkoutService.applyDiscount(orderNumber, request.discountCode());
    }

    @PostMapping("/orders/{orderNumber}/payment")
    public PaymentResponse submitPayment(
            @PathVariable String orderNumber,
            @Valid @RequestBody PaymentRequest request,
            Authentication auth,
            @RequestHeader(value = "X-Order-Token", required = false) String orderToken
    ) {
        checkoutService.verifyAccessTo(orderNumber, auth, orderToken);
        return checkoutService.submitPayment(orderNumber, request);
    }

    @GetMapping("/orders/{orderNumber}/confirmation")
    public ConfirmationResponse confirmation(
            @PathVariable String orderNumber,
            Authentication auth,
            @RequestHeader(value = "X-Order-Token", required = false) String orderToken
    ) {
        checkoutService.verifyAccessTo(orderNumber, auth, orderToken);
        return checkoutService.confirmation(orderNumber);
    }
}
