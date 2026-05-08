package com.paradise.event_ticket_system.viewEvent.api;

import com.paradise.event_ticket_system.viewEvent.api.DTO.TicketTypeRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.TicketTypeResponse;
import com.paradise.event_ticket_system.viewEvent.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Ticket Types", description = "Define and browse ticket options")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @GetMapping
    @Operation(summary = "Get ticket types for an event")
    @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypes(@PathVariable Integer eventId) {
        return ResponseEntity.ok(ticketTypeService.getTicketTypesForEvent(eventId));
    }

    @PostMapping
    @Operation(summary = "Create ticket type for an event")
    @ApiResponse(responseCode = "201", description = "Ticket type created successfully")
    @ApiResponse(responseCode = "404", description = "Event not found")
    public ResponseEntity<TicketTypeResponse> createTicketType(
            @PathVariable Integer eventId,
            @RequestBody @Valid TicketTypeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketTypeService.createTicketType(eventId, request));
    }
}
