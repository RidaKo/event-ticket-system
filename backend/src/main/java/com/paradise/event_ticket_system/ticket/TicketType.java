package com.paradise.event_ticket_system.ticket;

import com.paradise.event_ticket_system.event.Event;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Event event;

    private String name;
    private BigDecimal price;
    private int totalQuantity;
    private int soldQuantity;
    private boolean salesEnabled = true;

    public int getAvailableQuantity() {
        return Math.max(0, totalQuantity - soldQuantity);
    }
}
