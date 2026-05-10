package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("""
        SELECT r FROM Review r
        JOIN FETCH r.user
        WHERE r.event.id = :eventId
    """)
    List<Review> findByEventIdWithUser(Integer eventId);
}