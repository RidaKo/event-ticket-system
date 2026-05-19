package com.paradise.event_ticket_system.auth;

import com.paradise.event_ticket_system.auth.dto.AuthResponse;
import com.paradise.event_ticket_system.auth.dto.CurrentUserResponse;
import com.paradise.event_ticket_system.auth.dto.LoginRequest;
import com.paradise.event_ticket_system.auth.dto.RegisterRequest;
import com.paradise.event_ticket_system.model.User;
import com.paradise.event_ticket_system.viewEvent.domain.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setIsGuest(false);
        user.setRole(UserRole.USER);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (user.getRole() == UserRole.GUEST || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse currentUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.issue(user.getEmail(), user.getRole());
        return new AuthResponse(
                token,
                jwtService.expirationOf(token),
                new CurrentUserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole())
        );
    }
}
