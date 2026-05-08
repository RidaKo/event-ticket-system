package com.paradise.event_ticket_system.ticket;

import com.paradise.event_ticket_system.model.TicketType;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketTypeRepository extends JpaRepository<TicketType, Integer> {

    List<TicketType> findByEventIdOrderById(Integer eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TicketType t where t.id in :ids")
    List<TicketType> findAllByIdForUpdate(@Param("ids") Collection<Integer> ids);
}
