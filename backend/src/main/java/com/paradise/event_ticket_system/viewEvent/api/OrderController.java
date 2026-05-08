package com.paradise.event_ticket_system.viewEvent.api;

import com.paradise.event_ticket_system.viewEvent.api.DTO.OrderRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.OrderResponse;
import com.paradise.event_ticket_system.viewEvent.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Register ticket orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create an order")
    @ApiResponse(responseCode = "201", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Order validation failed")
    @ApiResponse(responseCode = "404", description = "Event, ticket type, or user not found")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }
}
