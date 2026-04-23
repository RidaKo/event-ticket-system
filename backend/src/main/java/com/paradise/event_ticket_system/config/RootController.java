package com.paradise.event_ticket_system.config;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
                "status", "backend running",
                "checkout", "start the React frontend and open http://127.0.0.1:5173/events/1/checkout/tickets",
                "sampleApi", "/api/events/1"
        );
    }
}
