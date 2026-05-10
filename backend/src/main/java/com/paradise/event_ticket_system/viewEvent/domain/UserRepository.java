package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
