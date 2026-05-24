package com.paradise.event_ticket_system.order;

import com.paradise.event_ticket_system.model.PurchaseOrder;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findByUser_EmailIgnoreCaseAndStatusOrderByConfirmedAtDescCreatedAtDesc(
            String email,
            OrderStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from PurchaseOrder o where o.orderNumber = :orderNumber")
    Optional<PurchaseOrder> findByOrderNumberForUpdate(@Param("orderNumber") String orderNumber);
}
