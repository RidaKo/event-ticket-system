package com.paradise.event_ticket_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_phone", length = 50)
    private String guestPhone;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "fees", nullable = false, precision = 12, scale = 2)
    private BigDecimal fees;

    @Column(name = "tax", nullable = false, precision = 12, scale = 2)
    private BigDecimal tax;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "payment_provider", length = 50)
    private String paymentProvider;

    @Column(name = "payment_reference")
    private String paymentReference;
}
