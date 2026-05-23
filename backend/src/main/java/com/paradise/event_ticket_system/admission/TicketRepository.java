package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.model.Ticket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    boolean existsByPurchaseOrderId(Long purchaseOrderId);

    List<Ticket> findByPurchaseOrderIdOrderByIdAsc(Long purchaseOrderId);

    Optional<Ticket> findByTicketCode(String ticketCode);

    @Query("""
        SELECT t FROM Ticket t
        JOIN FETCH t.ticketType
        JOIN FETCH t.event
        WHERE t.ticketCode = :ticketCode
        """)
    Optional<Ticket> findByTicketCodeWithDetails(@Param("ticketCode") String ticketCode);
}
