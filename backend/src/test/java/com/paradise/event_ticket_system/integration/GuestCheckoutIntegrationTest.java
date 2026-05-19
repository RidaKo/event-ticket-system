package com.paradise.event_ticket_system.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class GuestCheckoutIntegrationTest {

    @Autowired MockMvc mvc;
    final ObjectMapper json = new ObjectMapper();

    @Test
    void guestCanCreateOrderWithoutLogin() throws Exception {
        String body = """
            {
              "eventId": 1,
              "guestName": "Guest Tester",
              "guestEmail": "guest.tester@example.com",
              "items": [{ "ticketTypeId": 1, "quantity": 2 }]
            }
            """;

        String response = mvc.perform(post("/api/checkout/orders/guest")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").exists())
                .andExpect(jsonPath("$.guestEmail").value("guest.tester@example.com"))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = json.readTree(response);
        String orderNumber = node.get("orderNumber").asText();

        mvc.perform(get("/api/checkout/orders/" + orderNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(orderNumber));
    }

    @Test
    void authenticatedOrdersEndpointRequiresAuth() throws Exception {
        mvc.perform(post("/api/checkout/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "eventId": 1, "items": [{ "ticketTypeId": 1, "quantity": 1 }] }
                            """))
                .andExpect(status().isUnauthorized());
    }
}
