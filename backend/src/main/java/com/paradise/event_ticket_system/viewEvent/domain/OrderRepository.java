package com.paradise.event_ticket_system.viewEvent.domain;

import com.paradise.event_ticket_system.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
