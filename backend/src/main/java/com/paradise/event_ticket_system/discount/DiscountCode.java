package com.paradise.event_ticket_system.discount;

import com.paradise.event_ticket_system.model.Event;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Event event;

    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType type;

    @Column(name = "discount_value")
    private BigDecimal value;
    private boolean active = true;
    private Integer maxRedemptions;
    private int usedCount;
    private LocalDateTime expiresAt;

    public boolean hasRedemptionsLeft() {
        return maxRedemptions == null || usedCount < maxRedemptions;
    }
}
