package com.paradise.event_ticket_system.ticket;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventIdOrderById(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from CheckoutTicketType t where t.id in :ids")
    List<TicketType> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
