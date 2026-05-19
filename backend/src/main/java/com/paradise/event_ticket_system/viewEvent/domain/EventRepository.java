package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Integer> {

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

    @Query("""
           SELECT DISTINCT e
           FROM Event e
           LEFT JOIN FETCH e.tags
           JOIN FETCH e.venue v
           JOIN FETCH e.category c
           WHERE UPPER(e.status) NOT IN :closedStatuses
             AND e.startDatetime > :now
           """)
    List<Event> findUpcomingDiscoverable(@Param("now") Instant now,
                                         @Param("closedStatuses") List<String> closedStatuses);
}