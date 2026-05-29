package com.paradise.event_ticket_system.recommendation;

import com.paradise.event_ticket_system.model.Event;
import com.paradise.event_ticket_system.user.UserPreferences;
import java.time.Instant;

public interface RecommendationScoringStrategy {

    int score(Event event, UserPreferences preferences, Instant now);
}
