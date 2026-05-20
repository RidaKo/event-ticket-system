package com.paradise.event_ticket_system.category;

import com.paradise.event_ticket_system.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getIconUrl()
        );
    }

    public Category toEntity(CategoryRequest request) {
        Category category = new Category();
        category.setName(request.name());
        category.setSlug(request.slug());
        category.setIconUrl(request.iconUrl());
        return category;
    }
}