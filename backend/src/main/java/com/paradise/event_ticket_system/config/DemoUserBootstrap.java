package com.paradise.event_ticket_system.config;

import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Wires the demo recommendations user after Flyway seed scripts run.
 */
@Component
public class DemoUserBootstrap implements CommandLineRunner {

    private static final String DEMO_EMAIL = "alex@demo.local";

    private final UserRepository userRepository;
    private final DemoUserProvider demoUserProvider;

    public DemoUserBootstrap(UserRepository userRepository, DemoUserProvider demoUserProvider) {
        this.userRepository = userRepository;
        this.demoUserProvider = demoUserProvider;
    }

    @Override
    public void run(String... args) {
        userRepository.findAll().stream()
                .filter(user -> DEMO_EMAIL.equalsIgnoreCase(user.getEmail()))
                .findFirst()
                .ifPresent(user -> demoUserProvider.setDemoUserId(user.getId()));
    }
}
