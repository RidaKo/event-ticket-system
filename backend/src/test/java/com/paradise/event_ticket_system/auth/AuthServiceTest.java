package com.paradise.event_ticket_system.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paradise.event_ticket_system.auth.dto.AuthResponse;
import com.paradise.event_ticket_system.auth.dto.LoginRequest;
import com.paradise.event_ticket_system.auth.dto.RegisterRequest;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthService service;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        JwtService jwtService = new JwtService("test-secret-test-secret-test-secret-32+chars", Duration.ofMinutes(60));
        service = new AuthService(userRepository, new BCryptPasswordEncoder(4), jwtService);
    }

    @Test
    void registerHashesPasswordAndDefaultsToUserRole() {
        Mockito.when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(false);
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42);
            return u;
        });

        AuthResponse response = service.register(new RegisterRequest("Alice", "alice@example.com", "password1", null));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getPasswordHash()).isNotEqualTo("password1");
        assertThat(new BCryptPasswordEncoder(4).matches("password1", saved.getPasswordHash())).isTrue();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(response.user().email()).isEqualTo("alice@example.com");
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void registerRejectsDuplicateEmail() {
        Mockito.when(userRepository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("Alice", "alice@example.com", "password1", null)))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void loginRejectsBadPassword() {
        User user = new User();
        user.setId(1);
        user.setEmail("alice@example.com");
        user.setFullName("Alice");
        user.setRole(UserRole.USER);
        user.setPasswordHash(new BCryptPasswordEncoder(4).encode("realpassword"));
        Mockito.when(userRepository.findByEmailIgnoreCase("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("alice@example.com", "wrong")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void loginRejectsGuestAccount() {
        User user = new User();
        user.setEmail("guest@example.com");
        user.setRole(UserRole.GUEST);
        user.setIsGuest(true);
        Mockito.when(userRepository.findByEmailIgnoreCase("guest@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("guest@example.com", "anything")))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
