package ru.mirakyan.mymarket.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.repository.UserRepository;
import ru.mirakyan.mymarket.service.PaymentClient;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public PaymentClient testPaymentClient() {
        PaymentClient paymentClient = mock(PaymentClient.class);
        
        when(paymentClient.getBalance()).thenReturn(Mono.just(10000L));
        when(paymentClient.isServiceAvailable()).thenReturn(Mono.just(true));
        when(paymentClient.processPayment(anyLong(), anyLong(), anyString()))
                .thenReturn(Mono.just(true));
        
        return paymentClient;
    }
    
    @Bean
    @Primary
    public UserRepository testUserRepository() {
        UserRepository userRepository = mock(UserRepository.class);
        
        ru.mirakyan.mymarket.model.User testUser = new ru.mirakyan.mymarket.model.User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQNkhJqcxr1.cFtQb4R7m"); // password
        testUser.setEnabled(true);
        
        when(userRepository.findByUsername("testuser")).thenReturn(Mono.just(testUser));
        when(userRepository.findById(1L)).thenReturn(Mono.just(testUser));
        
        return userRepository;
    }
}
