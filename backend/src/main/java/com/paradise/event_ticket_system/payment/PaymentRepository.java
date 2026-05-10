package com.paradise.event_ticket_system.payment;

import com.paradise.event_ticket_system.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
