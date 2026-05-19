package com.paradise.event_ticket_system.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String DEFAULT_SECRET = "dev-secret-change-me-please-32-chars-or-more";

    private final SecretKey key;
    private final Duration ttl;

    @Autowired
    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this(secret, Duration.ofMinutes(expirationMinutes));
    }

    public JwtService(String secret, Duration ttl) {
        if (DEFAULT_SECRET.equals(secret)) {
            log.warn("Using default JWT secret. Set APP_JWT_SECRET before deploying to any shared environment.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttl = ttl;
    }

    public String issue(String subject, UserRole role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Instant expirationOf(String token) {
        return parseInternal(token).getExpiration().toInstant();
    }

    public Claims parse(String token) {
        var c = parseInternal(token);
        return new Claims(c.getSubject(), UserRole.valueOf((String) c.get("role")));
    }

    private io.jsonwebtoken.Claims parseInternal(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record Claims(String subject, UserRole role) {}
}
