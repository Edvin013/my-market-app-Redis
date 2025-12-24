package ru.mirakyan.mymarket.payment.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;

/**
 * Утилита для работы с безопасностью в реактивном контексте
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(jwt -> jwt.getClaimAsString("preferred_username"))
                .switchIfEmpty(Mono.error(new IllegalStateException("Пользователь не аутентифицирован")));
    }

}

