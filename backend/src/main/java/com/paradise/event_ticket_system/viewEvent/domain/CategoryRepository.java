package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {}
