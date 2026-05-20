package com.paradise.event_ticket_system.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("seed")
class CatalogControllerIntegrationTest {

    @Autowired
    private CatalogController catalogController;

    @Test
    void categoriesEndpointReturnsFrontendReadyOptions() {
        var categories = catalogController.categories();

        assertEquals(5, categories.size());
        assertEquals("ARTS", categories.getFirst().value());
        assertEquals("Arts", categories.getFirst().label());
    }

    @Test
    void tagsEndpointReturnsSeededTagsFromDatabase() {
        var tags = catalogController.tags();

        assertEquals(4, tags.size());
        assertEquals("educational", tags.getFirst().slug());
        assertEquals("Educational", tags.getFirst().label());
        assertFalse(tags.stream().anyMatch(tag -> tag.id() == null));
    }
}
