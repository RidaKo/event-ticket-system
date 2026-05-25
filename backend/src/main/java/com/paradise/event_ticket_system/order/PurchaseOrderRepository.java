package com.paradise.event_ticket_system.order;

import com.paradise.event_ticket_system.model.PurchaseOrder;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    @Query(
            value = """
                    select o.id
                    from PurchaseOrder o
                    join o.user u
                    where lower(u.email) = lower(:email)
                      and o.status = :status
                    order by o.confirmedAt desc, o.createdAt desc, o.id desc
                    """,
            countQuery = """
                    select count(o.id)
                    from PurchaseOrder o
                    join o.user u
                    where lower(u.email) = lower(:email)
                      and o.status = :status
                    """
    )
    Page<Long> findOrderListPageIdsByUserEmailIgnoreCaseAndStatus(
            @Param("email") String email,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query("""
            select distinct o
            from PurchaseOrder o
            join fetch o.event e
            join fetch e.venue
            left join fetch o.payment
            left join fetch o.discountCode
            left join fetch o.items i
            left join fetch i.ticketType
            where o.id in :ids
            """)
    List<PurchaseOrder> findOrderListItemsByIdIn(@Param("ids") Collection<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o where o.orderNumber = :orderNumber")
    Optional<PurchaseOrder> findByOrderNumberForUpdate(@Param("orderNumber") String orderNumber);
}
