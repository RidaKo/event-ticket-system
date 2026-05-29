package com.paradise.event_ticket_system.user;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("seed")
class UserPreferencesIntegrationTest {

    @Autowired
    MockMvc mvc;

    final ObjectMapper json = new ObjectMapper();

    @Test
    void authenticatedUserCanSaveAndLoadPreferences() throws Exception {
        String email = "prefs-user@example.com";
        String token = registerAndGetToken(email);

        mvc.perform(get("/api/users/me/preferences").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorySlugs", hasSize(0)))
                .andExpect(jsonPath("$.tagSlugs", hasSize(0)));

        String body = """
                {
                  "categorySlugs": ["music", "technology"],
                  "tagSlugs": ["outdoor"],
                  "homeCity": "Vilnius"
                }
                """;

        mvc.perform(put("/api/users/me/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorySlugs", hasSize(2)))
                .andExpect(jsonPath("$.categorySlugs", hasItem("music")))
                .andExpect(jsonPath("$.tagSlugs", hasItem("outdoor")))
                .andExpect(jsonPath("$.homeCity").value("Vilnius"));

        mvc.perform(get("/api/users/me/preferences").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorySlugs", hasItem("technology")))
                .andExpect(jsonPath("$.homeCity").value("Vilnius"));
    }

    @Test
    void preferencesRequireAuthentication() throws Exception {
        mvc.perform(get("/api/users/me/preferences"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unknownCategorySlugIsRejected() throws Exception {
        String token = registerAndGetToken("prefs-bad@example.com");

        mvc.perform(put("/api/users/me/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "categorySlugs": ["not-a-real-category"], "tagSlugs": [] }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void staleSaveReturnsConflict() throws Exception {
        String token = registerAndGetToken("conflict-user@example.com");

        // First save — no version — creates preferences, response includes version=0
        mvc.perform(put("/api/users/me/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "categorySlugs": ["music"], "tagSlugs": [], "homeCity": "Vilnius" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(0));

        // Second save with correct version=0 — succeeds, version becomes 1
        mvc.perform(put("/api/users/me/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "categorySlugs": ["music"], "tagSlugs": [], "homeCity": "Kaunas", "version": 0 }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(1));

        // Third save with stale version=0 — returns 409
        mvc.perform(put("/api/users/me/preferences")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "categorySlugs": ["technology"], "tagSlugs": [], "homeCity": "Kaunas", "version": 0 }
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("OPTIMISTIC_LOCK_CONFLICT"))
                .andExpect(jsonPath("$.current.version").value(1))
                .andExpect(jsonPath("$.current.homeCity").value("Kaunas"));
    }

    private String registerAndGetToken(String email) throws Exception {
        String body = """
                { "fullName": "Prefs User", "email": "%s", "password": "password1" }
                """.formatted(email);

        String response = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode node = json.readTree(response);
        return node.get("token").asText();
    }
}
