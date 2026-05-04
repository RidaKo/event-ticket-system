package com.paradise.event_ticket_system.event.dto;

import com.paradise.event_ticket_system.event.Tag;

public record TagOptionDto(
        Integer id,
        String slug,
        String label
) {
    public static TagOptionDto from(Tag tag) {
        return new TagOptionDto(tag.getId(), tag.getSlug(), tag.getLabel());
    }
}
