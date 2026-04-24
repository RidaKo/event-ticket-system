package com.paradise.event_ticket_system.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
           SELECT DISTINCT e
           FROM Event e
           LEFT JOIN FETCH e.tags
           WHERE e.status = com.paradise.event_ticket_system.event.EventStatus.PUBLISHED
             AND e.startAt > :now
           """)
    List<Event> findUpcomingPublished(@Param("now") LocalDateTime now);
}
