package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VenueRepository extends JpaRepository<Venue, Integer> {

    Optional<Venue> findFirstByOrganizer_IdAndNameIgnoreCaseAndCityIgnoreCase(
            Integer organizerId,
            String name,
            String city
    );

    List<Venue> findByOrganizerId(Integer organizerId);
}
