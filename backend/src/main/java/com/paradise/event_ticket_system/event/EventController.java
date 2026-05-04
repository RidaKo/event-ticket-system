package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.config.DemoUserProvider;
import com.paradise.event_ticket_system.event.dto.CreateEventRequest;
import com.paradise.event_ticket_system.event.dto.EventSummaryDto;
import com.paradise.event_ticket_system.model.Event;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final DemoUserProvider demoUserProvider;

    public EventController(EventService eventService, DemoUserProvider demoUserProvider) {
        this.eventService = eventService;
        this.demoUserProvider = demoUserProvider;
    }

    @GetMapping
    public List<EventSummaryDto> mine() {
        Integer userId = demoUserProvider.getDemoUserId();
        return eventService.listForUser(userId).stream()
                .map(EventSummaryDto::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventSummaryDto create(@Valid @RequestBody CreateEventRequest request) {
        Integer userId = demoUserProvider.getDemoUserId();
        Event saved = eventService.create(userId, request);
        return EventSummaryDto.from(saved);
    }
}
