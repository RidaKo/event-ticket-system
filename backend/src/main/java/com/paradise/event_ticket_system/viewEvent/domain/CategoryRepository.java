package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findAllByOrderByNameAsc();

    Optional<Category> findBySlug(String slug);
}
