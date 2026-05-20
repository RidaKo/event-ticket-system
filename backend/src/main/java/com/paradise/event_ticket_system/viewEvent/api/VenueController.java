package com.paradise.event_ticket_system.viewEvent.api;

import com.paradise.event_ticket_system.viewEvent.api.DTO.EventResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueResponse;
import com.paradise.event_ticket_system.viewEvent.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Create and view venues")
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER','ADMIN')")
    @Operation(summary = "Create a venue")
    @ApiResponse(responseCode = "201", description = "Venue created successfully")
    public ResponseEntity<VenueResponse> createVenue(@RequestBody @Valid VenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.createVenue(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get venue by ID")
    @ApiResponse(responseCode = "200", description = "Venue found")
    @ApiResponse(responseCode = "404", description = "Venue not found")
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Integer id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @GetMapping
    @Operation(summary = "Get all Venues", description = "Returns a list of all Venues with")
    @ApiResponse(responseCode = "200", description = "Venues retrieved successfully")
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }
}