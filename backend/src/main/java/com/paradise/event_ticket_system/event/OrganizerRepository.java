package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.model.Organizer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrganizerRepository extends JpaRepository<Organizer, Integer> {
    Optional<Organizer> findByUser_Id(Integer userId);
}
