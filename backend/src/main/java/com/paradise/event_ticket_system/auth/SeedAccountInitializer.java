package com.paradise.event_ticket_system.auth;

import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("seed")
public class SeedAccountInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public SeedAccountInitializer(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        rewriteHash("admin@test.com", "admin123");
        rewriteHash("organizer@test.com", "organizer123");
    }

    private void rewriteHash(String email, String plaintext) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(u -> {
            if ("placeholder".equals(u.getPasswordHash())) {
                u.setPasswordHash(encoder.encode(plaintext));
                userRepository.save(u);
            }
        });
    }
}
