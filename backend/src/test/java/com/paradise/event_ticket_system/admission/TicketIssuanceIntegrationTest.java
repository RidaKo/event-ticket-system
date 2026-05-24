package com.paradise.event_ticket_system.admission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paradise.event_ticket_system.model.Ticket;
import com.paradise.event_ticket_system.order.PurchaseOrderRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("seed")
class TicketIssuanceIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    private final ObjectMapper json = new ObjectMapper();

    private String guestOrderToken;

    @Test
    void successfulGuestPaymentIssuesTicketsWithProtectedQrEndpoints() throws Exception {
        String orderNumber = createGuestOrderAndPay();

        Long orderId = purchaseOrderRepository.findByOrderNumber(orderNumber).orElseThrow().getId();
        List<Ticket> tickets = ticketRepository.findByPurchaseOrderIdOrderByIdAsc(orderId);
        assertThat(tickets).hasSize(1);

        Ticket ticket = tickets.getFirst();
        assertThat(ticket.getTicketCode()).startsWith("TKT-");

        mvc.perform(get("/api/tickets/" + ticket.getTicketCode() + "/qr"))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/tickets/" + ticket.getTicketCode() + "/qr")
                .header("X-Order-Token", guestOrderToken))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));

        mvc.perform(get("/api/tickets/verify/" + ticket.getTicketCode()))
            .andExpect(status().isForbidden());

        mvc.perform(get("/api/tickets/verify/" + ticket.getTicketCode())
                .header("X-Order-Token", guestOrderToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.ticketCode").value(ticket.getTicketCode()))
            .andExpect(jsonPath("$.status").value(TicketStatus.VALID));

        mvc.perform(get("/api/checkout/orders/" + orderNumber + "/confirmation")
                .header("X-Order-Token", guestOrderToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tickets").isArray())
            .andExpect(jsonPath("$.tickets[0].qrImageBase64").isNotEmpty());
    }

    private String createGuestOrderAndPay() throws Exception {
        String body = """
            {
              "eventId": 1,
              "guestName": "QR Guest",
              "guestEmail": "qr-guest@example.com",
              "items": [{ "ticketTypeId": 1, "quantity": 1 }]
            }
            """;

        String response = mvc.perform(post("/api/checkout/orders/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode node = json.readTree(response);
        String orderNumber = node.get("orderNumber").asText();
        guestOrderToken = node.get("orderToken").asText();

        mvc.perform(post("/api/checkout/orders/" + orderNumber + "/payment")
                .header("X-Order-Token", guestOrderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"methodType": "CARD", "cardNumber": "4242 4242 4242 4242"}
                    """))
            .andExpect(status().isOk());

        return orderNumber;
    }
}
