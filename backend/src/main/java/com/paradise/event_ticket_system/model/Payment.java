package com.paradise.event_ticket_system.model;

import com.paradise.event_ticket_system.payment.PaymentMethodType;
import com.paradise.event_ticket_system.payment.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private PurchaseOrder order;

    @Enumerated(EnumType.STRING)
    private PaymentMethodType methodType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    private BigDecimal amount;
    private String providerReference;
    private String cardLast4;
    private LocalDateTime createdAt = LocalDateTime.now();
}