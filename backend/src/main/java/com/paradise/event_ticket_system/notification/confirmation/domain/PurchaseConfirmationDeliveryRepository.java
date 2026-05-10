package com.paradise.event_ticket_system.notification.confirmation.domain;

import java.util.Optional;

import com.paradise.event_ticket_system.model.PurchaseConfirmationDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseConfirmationDeliveryRepository
	extends JpaRepository<PurchaseConfirmationDelivery, Long> {

	Optional<PurchaseConfirmationDelivery> findByOrderId(Long orderId);
}
