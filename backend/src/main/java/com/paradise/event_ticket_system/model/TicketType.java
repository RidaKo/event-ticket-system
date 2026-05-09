package com.paradise.event_ticket_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ticket_types")
public class TicketType extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "name", nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    @DecimalMin("0.01")
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "quantity_total", nullable = false)
    private Integer quantityTotal;

    @Column(name = "quantity_sold", nullable = false)
    private Integer quantitySold = 0;

    @Column(name = "sale_start")
    private Instant saleStart;

    @Column(name = "sale_end")
    private Instant saleEnd;

    @Column(name = "max_per_order", nullable = false)
    private Integer maxPerOrder = 10;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long version;
}
