package com.paradise.event_ticket_system.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.JwtException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-32+chars";

    @Test
    void signedTokenCanBeParsedAndReturnsSubjectAndRole() {
        JwtService service = new JwtService(SECRET, Duration.ofMinutes(60));

        String token = service.issue("alice@example.com", UserRole.USER);
        JwtService.Claims claims = service.parse(token);

        assertThat(claims.subject()).isEqualTo("alice@example.com");
        assertThat(claims.role()).isEqualTo(UserRole.USER);
    }

    @Test
    void expiredTokenIsRejected() throws InterruptedException {
        JwtService service = new JwtService(SECRET, Duration.ofMillis(1));
        String token = service.issue("alice@example.com", UserRole.USER);
        Thread.sleep(50);

        assertThatThrownBy(() -> service.parse(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(SECRET, Duration.ofMinutes(60));
        String token = service.issue("alice@example.com", UserRole.USER);
        String tampered = token.substring(0, token.length() - 2) + "AA";

        assertThatThrownBy(() -> service.parse(tampered)).isInstanceOf(JwtException.class);
    }
}
