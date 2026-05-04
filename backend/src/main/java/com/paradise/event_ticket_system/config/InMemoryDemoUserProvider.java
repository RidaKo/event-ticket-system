package com.paradise.event_ticket_system.config;

import org.springframework.stereotype.Component;

@Component
public class InMemoryDemoUserProvider implements DemoUserProvider {

    private Integer demoUserId;

    @Override
    public Integer getDemoUserId() {
        return demoUserId;
    }

    @Override
    public void setDemoUserId(Integer id) {
        this.demoUserId = id;
    }
}
