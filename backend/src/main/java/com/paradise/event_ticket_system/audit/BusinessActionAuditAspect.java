package com.paradise.event_ticket_system.audit;

import java.time.Instant;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(prefix = "event-ticket.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BusinessActionAuditAspect {

    private static final Logger log = LoggerFactory.getLogger(BusinessActionAuditAspect.class);

    @Around("@annotation(com.paradise.event_ticket_system.audit.AuditedBusinessAction)"
        + " || @within(com.paradise.event_ticket_system.audit.AuditedBusinessAction)")
    public Object auditBusinessAction(ProceedingJoinPoint joinPoint) throws Throwable {
        Instant timestamp = Instant.now();
        long startedAt = System.nanoTime();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget() == null
            ? signature.getDeclaringType().getSimpleName()
            : joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();
            log.info(
                "business_action timestamp={} username={} roles={} class={} method={} outcome=success durationMs={}",
                timestamp,
                username(authentication),
                roles(authentication),
                className,
                signature.getName(),
                elapsedMillis(startedAt)
            );
            return result;
        }
        catch (Throwable ex) {
            log.warn(
                "business_action timestamp={} username={} roles={} class={} method={} outcome=failure durationMs={} error={}: {}",
                timestamp,
                username(authentication),
                roles(authentication),
                className,
                signature.getName(),
                elapsedMillis(startedAt),
                ex.getClass().getSimpleName(),
                ex.getMessage()
            );
            throw ex;
        }
    }

    private String username(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
            || "anonymousUser".equals(authentication.getName())) {
            return "anonymous";
        }
        return authentication.getName();
    }

    private String roles(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "[]";
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .sorted()
            .collect(Collectors.joining(",", "[", "]"));
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
