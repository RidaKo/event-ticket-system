package com.paradise.event_ticket_system.viewEvent.service;

import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.Review;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.viewEvent.api.DTO.EventResponse;
import com.paradise.event_ticket_system.viewEvent.api.EventMapper;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueResponse;
import com.paradise.event_ticket_system.viewEvent.domain.OrganizerRepository;
import com.paradise.event_ticket_system.viewEvent.domain.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final OrganizerRepository organizerRepository;
    private final EventMapper eventMapper;

    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        Organizer organizer = organizerRepository.findById(request.organizerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organizer with id " + request.organizerId() + " not found"
                ));

        Venue venue = eventMapper.toVenueEntity(request, organizer);
        Venue saved = venueRepository.save(venue);
        return eventMapper.toVenueResponse(saved);
    }

    @Transactional(readOnly = true)
    public VenueResponse getVenueById(Integer id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Venue with id " + id + " not found"
                ));
        return eventMapper.toVenueResponse(venue);
    }
    @Transactional(readOnly = true)
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(eventMapper::toVenueResponse)
                .toList();
    }

}