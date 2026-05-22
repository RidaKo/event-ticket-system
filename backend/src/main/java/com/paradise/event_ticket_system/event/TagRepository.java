package com.paradise.event_ticket_system.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    List<Tag> findAllByOrderByLabelAsc();

    Optional<Tag> findBySlug(String slug);
}
