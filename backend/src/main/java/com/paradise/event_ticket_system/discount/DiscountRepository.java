package com.paradise.event_ticket_system.discount;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<DiscountCode, Long> {

    Optional<DiscountCode> findByCodeIgnoreCaseAndEventId(String code, Integer eventId);
}
