package com.paradise.event_ticket_system.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthEntryPoint entryPoint;
    private final RestAccessDeniedHandler deniedHandler;
    private final boolean devEndpointsEnabled;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter,
                          JwtAuthEntryPoint entryPoint,
                          RestAccessDeniedHandler deniedHandler,
                          @Value("${app.security.dev-endpoints-enabled:true}") boolean devEndpointsEnabled) {
        this.jwtFilter = jwtFilter;
        this.entryPoint = entryPoint;
        this.deniedHandler = deniedHandler;
        this.devEndpointsEnabled = devEndpointsEnabled;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))
                .authorizeHttpRequests(reg -> {
                    reg.requestMatchers("/", "/error").permitAll();
                    if (devEndpointsEnabled) {
                        reg.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll();
                        reg.requestMatchers("/h2-console/**").permitAll();
                    }
                    reg
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events", "/api/events/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/catalog/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/venues/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/*/ticket-types").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/checkout/quote").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/checkout/orders/guest").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/checkout/orders/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/checkout/orders/*/confirmation").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/checkout/orders/*/discount").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/checkout/orders/*/payment").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/*/qr").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tickets/verify/*").permitAll()
                        .anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
