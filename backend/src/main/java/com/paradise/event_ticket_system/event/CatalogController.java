package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.event.dto.CategoryOptionDto;
import com.paradise.event_ticket_system.event.dto.TagOptionDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogController {

    private final TagRepository tagRepository;

    public CatalogController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping("/categories")
    public List<CategoryOptionDto> categories() {
        return Arrays.stream(Category.values())
                .map(CategoryOptionDto::from)
                .sorted(Comparator.comparing(CategoryOptionDto::label))
                .toList();
    }

    @GetMapping("/tags")
    public List<TagOptionDto> tags() {
        return tagRepository.findAllByOrderByLabelAsc().stream()
                .map(TagOptionDto::from)
                .toList();
    }
}
