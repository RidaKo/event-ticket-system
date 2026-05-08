package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.model.Event;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {

    @EntityGraph(attributePaths = "venue")
    Optional<Event> findWithVenueById(Integer id);
}
