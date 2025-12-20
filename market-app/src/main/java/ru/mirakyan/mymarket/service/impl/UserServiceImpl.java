package ru.mirakyan.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.model.User;
import ru.mirakyan.mymarket.repository.UserRepository;
import ru.mirakyan.mymarket.security.SecurityUtils;
import ru.mirakyan.mymarket.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;

    @Override
    public Mono<Long> getCurrentUserId() {
        return getCurrentUser()
                .map(User::getId);
    }

    @Override
    public Mono<User> getCurrentUser() {
        return SecurityUtils.getCurrentUsername()
                .flatMap(userRepository::findByUsername);
    }
}

