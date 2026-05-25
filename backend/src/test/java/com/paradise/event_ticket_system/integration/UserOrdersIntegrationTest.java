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
class UserOrdersIntegrationTest {

    @Autowired MockMvc mvc;
    final ObjectMapper json = new ObjectMapper();

    @Test
    void authenticatedUserCanListCompletedOrdersAndOpenReceiptDetails() throws Exception {
        String token = register("orders-user@example.com", "Orders User");
        String otherToken = register("orders-other@example.com", "Orders Other");

        String confirmedOrderNumber = createOrder(token);
        payOrder(token, confirmedOrderNumber);
        createOrder(token);

        mvc.perform(get("/api/checkout/orders"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/checkout/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].orderNumber").value(confirmedOrderNumber))
                .andExpect(jsonPath("$.items[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$.items[0].event.title").value("Summer Fest"))
                .andExpect(jsonPath("$.items[0].summary.items[0].quantity").value(2));

        mvc.perform(get("/api/checkout/orders")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));

        awaitConfirmationEmailSent(token, confirmedOrderNumber);

        mvc.perform(get("/api/checkout/orders/" + confirmedOrderNumber + "/confirmation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value(confirmedOrderNumber))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.event.title").value("Summer Fest"))
                .andExpect(jsonPath("$.summary.items[0].name").value("General Admission"))
                .andExpect(jsonPath("$.paymentMethod").value("CARD"))
                .andExpect(jsonPath("$.cardLast4").value("4242"))
                .andExpect(jsonPath("$.confirmationEmail.status").value("SENT"))
                .andExpect(jsonPath("$.confirmationEmail.attendeeEmail").value("orders-user@example.com"));
    }

    @Test
    void authenticatedUserOrdersArePaginatedNewestFirst() throws Exception {
        String token = register("paged-orders-user@example.com", "Paged Orders User");

        String firstOrderNumber = createOrder(token);
        payOrder(token, firstOrderNumber);
        String secondOrderNumber = createOrder(token);
        payOrder(token, secondOrderNumber);
        String thirdOrderNumber = createOrder(token);
        payOrder(token, thirdOrderNumber);

        mvc.perform(get("/api/checkout/orders")
                        .param("page", "0")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].orderNumber").value(thirdOrderNumber))
                .andExpect(jsonPath("$.items[1].orderNumber").value(secondOrderNumber))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        mvc.perform(get("/api/checkout/orders")
                        .param("page", "1")
                        .param("size", "2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].orderNumber").value(firstOrderNumber))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true));
    }

    private String register(String email, String fullName) throws Exception {
        String response = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "fullName": "%s", "email": "%s", "password": "password1" }
                            """.formatted(fullName, email)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(response).get("token").asText();
    }

    private String createOrder(String token) throws Exception {
        String response = mvc.perform(post("/api/checkout/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "eventId": 1, "items": [{ "ticketTypeId": 1, "quantity": 2 }] }
                            """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return json.readTree(response).get("orderNumber").asText();
    }

    private void payOrder(String token, String orderNumber) throws Exception {
        mvc.perform(post("/api/checkout/orders/" + orderNumber + "/payment")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "methodType": "CARD", "cardNumber": "4242 4242 4242 4242" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("CONFIRMED"));
    }

    private void awaitConfirmationEmailSent(String token, String orderNumber) throws Exception {
        for (int attempt = 0; attempt < 30; attempt++) {
            String response = mvc.perform(get("/api/checkout/orders/" + orderNumber + "/confirmation")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            JsonNode confirmationEmail = json.readTree(response).path("confirmationEmail");
            if ("SENT".equals(confirmationEmail.path("status").asText())) {
                return;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Confirmation email was not sent for order " + orderNumber);
    }
}
