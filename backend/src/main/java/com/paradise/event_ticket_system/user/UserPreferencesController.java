package com.paradise.event_ticket_system.user;

import com.paradise.event_ticket_system.user.dto.UpdateUserPreferencesRequest;
import com.paradise.event_ticket_system.user.dto.UserPreferencesResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/preferences")
public class UserPreferencesController {

    private final UserPreferencesService preferencesService;

    public UserPreferencesController(UserPreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @GetMapping
    public UserPreferencesResponse getPreferences(Authentication authentication) {
        return preferencesService.getForEmail(authentication.getName());
    }

    @PutMapping
    public UserPreferencesResponse savePreferences(
            Authentication authentication,
            @Valid @RequestBody UpdateUserPreferencesRequest request
    ) {
        return preferencesService.saveForEmail(authentication.getName(), request);
    }
}
