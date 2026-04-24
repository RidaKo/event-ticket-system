package com.paradise.event_ticket_system.event.dto;

import com.paradise.event_ticket_system.event.Category;

public record CategoryOptionDto(
        String value,
        String label
) {
    public static CategoryOptionDto from(Category category) {
        return new CategoryOptionDto(category.name(), formatLabel(category.name()));
    }

    private static String formatLabel(String raw) {
        String normalized = raw.toLowerCase().replace('_', ' ');
        String[] parts = normalized.split(" ");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isBlank()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(parts[i].charAt(0)))
                    .append(parts[i].substring(1));
        }
        return result.toString();
    }
}
