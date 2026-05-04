package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {

    @Query("""
           SELECT DISTINCT e
           FROM Event e
           LEFT JOIN FETCH e.tags
           JOIN FETCH e.venue v
           JOIN FETCH e.category c
           WHERE e.status = :publishedStatus
             AND e.startDatetime > :now
           """)
    List<Event> findUpcomingPublished(@Param("now") Instant now,
                                      @Param("publishedStatus") String publishedStatus);

    @Query("""
           SELECT DISTINCT e
           FROM Event e
           LEFT JOIN FETCH e.tags
           JOIN FETCH e.venue v
           JOIN FETCH e.category c
           WHERE e.organizer.id = :organizerId
           ORDER BY e.createdAt DESC
           """)
    List<Event> findByOrganizerOrderByCreatedDesc(@Param("organizerId") Integer organizerId);
}
