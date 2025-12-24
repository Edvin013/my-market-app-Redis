package ru.mirakyan.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mirakyan.mymarket.repository.UserRepository;
import ru.mirakyan.mymarket.service.impl.UserServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private ru.mirakyan.mymarket.model.User testUser;

    @BeforeEach
    void setUp() {
        testUser = new ru.mirakyan.mymarket.model.User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setEnabled(true);
    }

    @Test
    void shouldGetCurrentUserById() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        StepVerifier.create(
                userService.getCurrentUserId()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        )
        .assertNext(userId -> assertThat(userId).isEqualTo(1L))
        .verifyComplete();
    }

    @Test
    void shouldGetCurrentUser() {
        // Настраиваем мок
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        // Создаем контекст безопасности
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Проверяем получение пользователя
        StepVerifier.create(
                userService.getCurrentUser()
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(securityContext)))
        )
        .assertNext(user -> {
            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getUsername()).isEqualTo("testuser");
        })
        .verifyComplete();
    }
}

