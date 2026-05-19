package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

    // Eagerly fetch all lazy relations in one query to avoid N+1
    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.venue
        JOIN FETCH e.organizer
        JOIN FETCH e.category
        WHERE e.id = :id
    """)
    Optional<Event> findByIdWithDetails(Integer id);

    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.venue
        JOIN FETCH e.organizer
        JOIN FETCH e.category
    """)
    List<Event> findAllWithDetails();

    List<Event> findByOrganizerId(Integer organizerId);
}