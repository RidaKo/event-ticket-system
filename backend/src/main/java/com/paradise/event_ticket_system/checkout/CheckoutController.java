package com.paradise.event_ticket_system.checkout;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/quote")
    public OrderSummaryResponse quote(@Valid @RequestBody CheckoutQuoteRequest request) {
        return checkoutService.quote(request);
    }

    @PostMapping("/orders")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return checkoutService.createOrder(request);
    }

    @GetMapping("/orders/{orderNumber}")
    public OrderResponse getOrder(@PathVariable String orderNumber) {
        return checkoutService.getOrder(orderNumber);
    }

    @PostMapping("/orders/{orderNumber}/discount")
    public OrderResponse applyDiscount(
            @PathVariable String orderNumber,
            @RequestBody ApplyDiscountRequest request
    ) {
        return checkoutService.applyDiscount(orderNumber, request.discountCode());
    }

    @PostMapping("/orders/{orderNumber}/payment")
    public PaymentResponse submitPayment(
            @PathVariable String orderNumber,
            @Valid @RequestBody PaymentRequest request
    ) {
        return checkoutService.submitPayment(orderNumber, request);
    }

    @GetMapping("/orders/{orderNumber}/confirmation")
    public ConfirmationResponse confirmation(@PathVariable String orderNumber) {
        return checkoutService.confirmation(orderNumber);
    }
}
