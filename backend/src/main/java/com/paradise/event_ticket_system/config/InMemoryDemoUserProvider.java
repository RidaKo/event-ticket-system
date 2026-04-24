package com.paradise.event_ticket_system.config;

import org.springframework.stereotype.Component;

@Component
public class InMemoryDemoUserProvider implements DemoUserProvider {

    private Long demoUserId;

    @Override
    public Long getDemoUserId() {
        return demoUserId;
    }

    @Override
    public void setDemoUserId(Long id) {
        this.demoUserId = id;
    }
}
