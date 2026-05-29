package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.event.dto.CategoryOptionDto;
import com.paradise.event_ticket_system.event.dto.TagOptionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    public List<CategoryOptionDto> categories() {
        return catalogService.categories();
    }

    @GetMapping("/tags")
    public List<TagOptionDto> tags() {
        return catalogService.tags();
    }
}
