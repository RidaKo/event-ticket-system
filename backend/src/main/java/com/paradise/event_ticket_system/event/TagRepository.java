package com.paradise.event_ticket_system.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    java.util.List<Tag> findAllByOrderByLabelAsc();

    Optional<Tag> findBySlug(String slug);
}
