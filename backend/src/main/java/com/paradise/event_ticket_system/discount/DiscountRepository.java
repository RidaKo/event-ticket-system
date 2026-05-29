package com.paradise.event_ticket_system.discount;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscountRepository extends JpaRepository<DiscountCode, Long> {

    Optional<DiscountCode> findByCodeIgnoreCaseAndEventId(String code, Integer eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DiscountCode d where d.id = :id")
    Optional<DiscountCode> findByIdForUpdate(@Param("id") Long id);
}
