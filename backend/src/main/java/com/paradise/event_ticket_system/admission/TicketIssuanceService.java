package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.model.OrderItem;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.model.TicketType;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.order.OrderStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketIssuanceService {

    private final TicketRepository ticketRepository;

    public TicketIssuanceService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public List<Ticket> issueForOrder(PurchaseOrder order) {
        if (order.getId() == null) {
            throw new IllegalArgumentException("Order must be persisted before issuing tickets");
        }
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tickets can only be issued for confirmed orders");
        }
        if (!StringUtils.hasText(order.getOrderToken())) {
            order.setOrderToken(UUID.randomUUID().toString());
        }
        if (ticketRepository.existsByPurchaseOrderId(order.getId())) {
            return ticketRepository.findByPurchaseOrderIdOrderByIdAsc(order.getId());
        }

        List<Ticket> issued = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            TicketType ticketType = item.getTicketType();
            BigDecimal unitPrice = item.getUnitPrice() == null
                ? BigDecimal.ZERO
                : item.getUnitPrice().setScale(2, RoundingMode.HALF_UP);
            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = new Ticket();
                ticket.setPurchaseOrder(order);
                ticket.setEvent(order.getEvent());
                ticket.setTicketType(ticketType);
                ticket.setOwnerUser(resolveOwnerUser(order));
                ticket.setOwnerEmail(resolveOwnerEmail(order));
                ticket.setOwnerName(resolveOwnerName(order));
                ticket.setPricePaid(unitPrice);
                String ticketCode = generateTicketCode();
                ticket.setTicketCode(ticketCode);
                ticket.setQrCodeUrl(null);
                ticket.setStatus(TicketStatus.VALID);
                issued.add(ticketRepository.save(ticket));
            }
        }
        return issued;
    }

    private User resolveOwnerUser(PurchaseOrder order) {
        User user = order.getUser();
        if (user != null && !Boolean.TRUE.equals(user.getIsGuest())) {
            return user;
        }
        return null;
    }

    private String resolveOwnerEmail(PurchaseOrder order) {
        if (order.getGuestEmail() != null && !order.getGuestEmail().isBlank()) {
            return order.getGuestEmail();
        }
        if (order.getUser() != null && order.getUser().getEmail() != null) {
            return order.getUser().getEmail();
        }
        throw new IllegalStateException("Order %s has no owner email".formatted(order.getId()));
    }

    private String resolveOwnerName(PurchaseOrder order) {
        if (order.getGuestName() != null && !order.getGuestName().isBlank()) {
            return order.getGuestName();
        }
        if (order.getUser() != null && order.getUser().getFullName() != null) {
            return order.getUser().getFullName();
        }
        return "Ticket holder";
    }

    private static String generateTicketCode() {
        return "TKT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
