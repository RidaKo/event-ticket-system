package com.paradise.event_ticket_system.integration;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
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
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mvc;
    final ObjectMapper json = new ObjectMapper();

    @Test
    void registerThenLoginThenMe() throws Exception {
        String body = """
            { "fullName": "Test User", "email": "test1@example.com", "password": "password1" }
            """;

        String registerResponse = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = json.readTree(registerResponse);
        String token = node.get("token").asText();

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "email": "test1@example.com", "password": "password1" }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test1@example.com"))
                .andExpect(jsonPath("$.id", greaterThan(0)));
    }

    @Test
    void duplicateRegisterReturns409() throws Exception {
        String body = """
            { "fullName": "Test User", "email": "dup@example.com", "password": "password1" }
            """;
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loginWithBadPasswordReturns401() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "fullName": "Test User", "email": "badpw@example.com", "password": "password1" }
                            """))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            { "email": "badpw@example.com", "password": "wrong" }
                            """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meWithoutTokenReturns401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }
}
