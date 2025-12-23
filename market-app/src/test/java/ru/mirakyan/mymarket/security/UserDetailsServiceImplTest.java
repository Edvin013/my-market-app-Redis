package ru.mirakyan.mymarket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mirakyan.mymarket.model.User;
import ru.mirakyan.mymarket.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setEnabled(true);
    }

    @Test
    void shouldLoadUserByUsername() {
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        StepVerifier.create(userDetailsService.findByUsername("testuser"))
                .assertNext(userDetails -> {
                    assertThat(userDetails.getUsername()).isEqualTo("testuser");
                    assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
                    assertThat(userDetails.isEnabled()).isTrue();
                    assertThat(userDetails.getAuthorities()).hasSize(1);
                })
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("nonexistent"))
                .thenReturn(Mono.empty());

        StepVerifier.create(userDetailsService.findByUsername("nonexistent"))
                .expectError(UsernameNotFoundException.class)
                .verify();
    }

    @Test
    void shouldLoadDisabledUser() {
        testUser.setEnabled(false);
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Mono.just(testUser));

        StepVerifier.create(userDetailsService.findByUsername("testuser"))
                .assertNext(userDetails -> {
                    assertThat(userDetails.getUsername()).isEqualTo("testuser");
                    assertThat(userDetails.isEnabled()).isFalse();
                })
                .verifyComplete();
    }
}

