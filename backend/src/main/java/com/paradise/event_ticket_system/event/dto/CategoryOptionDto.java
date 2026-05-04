package com.paradise.event_ticket_system.event.dto;

import com.paradise.event_ticket_system.model.Category;

import java.util.Locale;

public record CategoryOptionDto(
        String value,
        String label
) {
    public static CategoryOptionDto from(Category category) {
        return new CategoryOptionDto(category.getSlug().toUpperCase(Locale.ROOT), category.getName());
    }
}
