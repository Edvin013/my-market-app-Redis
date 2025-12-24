package ru.mirakyan.mymarket.service;

import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.model.User;

public interface UserService {
    /**
     * Получить ID текущего аутентифицированного пользователя
     */
    Mono<Long> getCurrentUserId();
    
    /**
     * Получить текущего аутентифицированного пользователя
     */
    Mono<User> getCurrentUser();
}

