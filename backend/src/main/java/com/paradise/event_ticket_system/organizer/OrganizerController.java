package com.paradise.event_ticket_system.organizer;

import com.paradise.event_ticket_system.viewEvent.api.DTO.EventResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.UpdateEventStatusRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueResponse;
import com.paradise.event_ticket_system.viewEvent.service.EventService;
import com.paradise.event_ticket_system.viewEvent.service.VenueService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizers")
public class OrganizerController {

    private final EventService eventService;
    private final VenueService venueService;

    public OrganizerController(EventService eventService, VenueService venueService) {
        this.eventService = eventService;
        this.venueService = venueService;
    }

    @GetMapping("/{organizerId}/events")
    @ApiResponse(responseCode = "200", description = "Organizer events retrieved successfully")
    public ResponseEntity<List<EventResponse>> getEventsByOrganizerId(
            @PathVariable Integer organizerId) {
        return ResponseEntity.ok(eventService.getEventsByOrganizerId(organizerId));
    }

    @GetMapping("/{organizerId}/venues")
    public ResponseEntity<List<VenueResponse>> getVenuesByOrganizerId(
            @PathVariable Integer organizerId) {
        return ResponseEntity.ok(venueService.getVenuesByOrganizerId(organizerId));
    }

    @PatchMapping("/{organizerId}/events/{eventId}/status")
    public ResponseEntity<Void> updateEventStatus(
            @PathVariable Integer organizerId,
            @PathVariable Integer eventId,
            @Valid @RequestBody UpdateEventStatusRequest request
    ) {
        eventService.updateStatus(
                organizerId,
                eventId,
                request.status(),
                request.version(),
                Boolean.TRUE.equals(request.force()));
        return ResponseEntity.ok().build();
    }
}
