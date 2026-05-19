package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VenueRepository extends JpaRepository<Venue, Integer> {
    List<Venue> findByOrganizerId(Integer organizerId);}
