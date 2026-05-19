package com.paradise.event_ticket_system.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paradise.event_ticket_system.auth.JwtService;
import com.paradise.event_ticket_system.auth.UserRole;
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
class AuthorizationIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    final ObjectMapper json = new ObjectMapper();

    private String tokenFor(String email, UserRole role) {
        return jwtService.issue(email, role);
    }

    @Test
    void publicGetEventsAlwaysWorks() throws Exception {
        mvc.perform(get("/api/events")).andExpect(status().isOk());
    }

    @Test
    void anonymousCannotCreateEvent() throws Exception {
        mvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCannotCreateEvent() throws Exception {
        String reg = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          { "fullName": "U", "email": "u1@example.com", "password": "password1" }
                          """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String token = json.readTree(reg).get("token").asText();

        String validBody = """
            {
              "venueId": 1,
              "organizerId": 1,
              "categoryId": 1,
              "title": "Test Event",
              "slug": "test-event-auth",
              "status": "ACTIVE",
              "startDatetime": "2026-12-01T10:00:00Z",
              "endDatetime": "2026-12-01T12:00:00Z",
              "timezone": "Europe/Vilnius"
            }
            """;

        mvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isForbidden());
    }

    @Test
    void organizerTokenCanReachVenueCreate() throws Exception {
        String token = tokenFor("organizer@test.com", UserRole.ORGANIZER);
        int statusCode = mvc.perform(post("/api/venues")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andReturn().getResponse().getStatus();
        org.assertj.core.api.Assertions.assertThat(statusCode).isNotEqualTo(403);
        org.assertj.core.api.Assertions.assertThat(statusCode).isNotEqualTo(401);
    }

    @Test
    void userCannotReadAnotherUsersOrder() throws Exception {
        String regA = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          { "fullName": "A", "email": "a@example.com", "password": "password1" }
                          """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String tokenA = json.readTree(regA).get("token").asText();

        String orderResp = mvc.perform(post("/api/checkout/orders")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          { "eventId": 1, "items": [{ "ticketTypeId": 1, "quantity": 1 }] }
                          """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String orderNumber = json.readTree(orderResp).get("orderNumber").asText();

        String regB = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          { "fullName": "B", "email": "b@example.com", "password": "password1" }
                          """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String tokenB = json.readTree(regB).get("token").asText();

        mvc.perform(get("/api/checkout/orders/" + orderNumber)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadAnyOrder() throws Exception {
        String regA = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          { "fullName": "A2", "email": "a2@example.com", "password": "password1" }
                          """))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String tokenA = json.readTree(regA).get("token").asText();

        String orderResp = mvc.perform(post("/api/checkout/orders")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                          { "eventId": 1, "items": [{ "ticketTypeId": 1, "quantity": 1 }] }
                          """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String orderNumber = json.readTree(orderResp).get("orderNumber").asText();

        String adminToken = tokenFor("admin@test.com", UserRole.ADMIN);

        mvc.perform(get("/api/checkout/orders/" + orderNumber)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
