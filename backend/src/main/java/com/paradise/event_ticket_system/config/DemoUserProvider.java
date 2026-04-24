package com.paradise.event_ticket_system.config;

/**
 * Stubbed "who is the current user" for MVP. Replaced by a real auth-aware
 * provider once Spring Security / session auth lands.
 */
public interface DemoUserProvider {
    Long getDemoUserId();

    void setDemoUserId(Long id);
}
