package com.paradise.event_ticket_system.viewEvent.api;

import com.paradise.event_ticket_system.event.EventStatus;
import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.model.Review;
import com.paradise.event_ticket_system.model.Venue;
import com.paradise.event_ticket_system.model.Organizer;
import com.paradise.event_ticket_system.model.Category;
import com.paradise.event_ticket_system.viewEvent.api.DTO.EventResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.ReviewResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueResponse;
import com.paradise.event_ticket_system.viewEvent.api.DTO.VenueRequest;
import com.paradise.event_ticket_system.viewEvent.api.DTO.EventRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventMapper {

    public EventResponse toEventResponse(Event event, List<Review> reviews) {
        double avgRating = reviews.stream()
                .mapToDouble(r -> r.getRating().doubleValue())
                .average()
                .orElse(0.0);

        return new EventResponse(
                event.getId(),
                event.getVersion(),
                event.getTitle(),
                event.getSlug(),
                event.getDescription(),
                event.getStatus(),
                event.getStartDatetime(),
                event.getEndDatetime(),
                event.getTimezone(),
                event.getMinAge(),
                event.getCoverPhotoUrl(),
                event.getPhotoUrls(),
                event.getCategory().getName(),
                event.getOrganizer().getBusinessName(),
                event.getVenue().getName(),
                event.getVenue().getAddressLine1(),
                event.getVenue().getCity(),
                event.getVenue().getCountry(),
                toVenueResponse(event.getVenue()),
                Math.round(avgRating * 10.0) / 10.0,
                reviews.size(),
                reviews.stream().map(this::toReviewResponse).toList()
        );
    }

    public VenueResponse toVenueResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getOrganizer().getBusinessName(),
                venue.getName(),
                venue.getAddressLine1(),
                venue.getCity(),
                venue.getCountry(),
                venue.getRating(),
                venue.getCoverPhotoUrl()
        );
    }

    private ReviewResponse toReviewResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
    public Venue toVenueEntity(VenueRequest request, Organizer organizer) {
        Venue venue = new Venue();
        venue.setOrganizer(organizer);
        venue.setName(request.name());
        venue.setAddressLine1(request.addressLine1());
        venue.setCity(request.city());
        venue.setCountry(request.country());
        venue.setCoverPhotoUrl(request.coverPhotoUrl());
        return venue;
    }

    public Event toEventEntity(EventRequest request, Organizer organizer, Venue venue, Category category) {
        Event event = new Event();
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setCategory(category);
        event.setTitle(request.title());
        event.setSlug(request.slug());
        event.setDescription(request.description());
        event.setStatus(EventStatus.valueOf(String.valueOf(request.status())));
        event.setStartDatetime(request.startDatetime());
        event.setEndDatetime(request.endDatetime());
        event.setTimezone(request.timezone());
        event.setMinAge(request.minAge());
        event.setCoverPhotoUrl(request.coverPhotoUrl());
        event.setPhotoUrls(request.photoUrls());
        return event;
    }
}
