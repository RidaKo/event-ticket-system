package com.paradise.event_ticket_system.event;

import com.paradise.event_ticket_system.audit.AuditedBusinessAction;
import com.paradise.event_ticket_system.category.CategoryRepository;
import com.paradise.event_ticket_system.event.dto.CategoryOptionDto;
import com.paradise.event_ticket_system.event.dto.TagOptionDto;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AuditedBusinessAction
public class CatalogService {

    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;

    public CatalogService(TagRepository tagRepository, CategoryRepository categoryRepository) {
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryOptionDto> categories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryOptionDto::from)
                .sorted(Comparator.comparing(CategoryOptionDto::label))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagOptionDto> tags() {
        return tagRepository.findAllByOrderByLabelAsc().stream()
                .map(TagOptionDto::from)
                .toList();
    }
}
