package com.paradise.event_ticket_system.admission;

import com.paradise.event_ticket_system.auth.UserRole;
import com.paradise.event_ticket_system.model.PurchaseOrder;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketAccessService {

    public void verifyAccess(Ticket ticket, Authentication auth, String orderToken) {
        PurchaseOrder order = ticket.getPurchaseOrder();
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ticket is not linked to an order");
        }
        if (isAdmin(auth)) {
            return;
        }
        if (orderToken != null && orderToken.equals(order.getOrderToken())) {
            return;
        }
        User owner = order.getUser();
        if (owner == null || owner.getRole() == UserRole.GUEST) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or missing order token");
        }
        String email = auth == null ? null : auth.getName();
        if (email == null || !email.equalsIgnoreCase(owner.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this ticket");
        }
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.isAuthenticated()
            && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
