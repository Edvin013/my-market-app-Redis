package ru.mirakyan.mymarket.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

public class SecurityUtils {
    
    /**
     * Получить имя текущего аутентифицированного пользователя
     */
    public static Mono<String> getCurrentUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(UserDetails.class)
                .map(UserDetails::getUsername);
    }
    
    /**
     * Проверить, аутентифицирован ли пользователь
     */
    public static Mono<Boolean> isAuthenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::isAuthenticated)
                .defaultIfEmpty(false);
    }
}

