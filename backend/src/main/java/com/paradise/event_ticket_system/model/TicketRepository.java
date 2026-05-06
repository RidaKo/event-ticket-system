package com.paradise.event_ticket_system.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {

    @Query("""
        select t
        from Ticket t
        join fetch t.order o
        left join fetch o.user
        join fetch t.event e
        join fetch e.venue
        join fetch t.ticketType tt
        where o.id = :orderId
        order by tt.name asc, t.id asc
        """)
    List<Ticket> findAllForPurchaseConfirmation(@Param("orderId") Integer orderId);
}
