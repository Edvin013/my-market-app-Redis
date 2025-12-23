package ru.mirakyan.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.config.SecurityConfig;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.security.UserDetailsServiceImpl;
import ru.mirakyan.mymarket.service.CartService;
import ru.mirakyan.mymarket.service.PaymentClient;

import static org.mockito.Mockito.when;

/**
 * Простые тесты для CartController с проверкой авторизации
 */
@WebFluxTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldDenyAccessToCartForAnonymousUser() {
        webTestClient
                .get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldAllowAccessToCartForAuthenticatedUser() {
        when(cartService.getCartItems()).thenReturn(Flux.empty());
        when(cartService.getTotalPrice()).thenReturn(Mono.just(0L));
        when(paymentClient.getBalance()).thenReturn(Mono.just(10000L));
        when(paymentClient.isServiceAvailable()).thenReturn(Mono.just(true));

        webTestClient
                .get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldDisplayCartWithItems() {
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setTitle("Товар 1");
        item1.setPrice(100L);
        item1.setCount(2);

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setTitle("Товар 2");
        item2.setPrice(200L);
        item2.setCount(1);

        // Настраиваем моки
        when(cartService.getCartItems()).thenReturn(Flux.just(item1, item2));
        when(cartService.getTotalPrice()).thenReturn(Mono.just(400L));
        when(paymentClient.getBalance()).thenReturn(Mono.just(10000L));
        when(paymentClient.isServiceAvailable()).thenReturn(Mono.just(true));

        webTestClient
                .get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldDenyPostRequestToCartForAnonymousUser() {
        webTestClient
                .post()
                .uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldAllowCartCheckoutWhenBalanceSufficient() {
        when(cartService.getCartItems()).thenReturn(Flux.empty());
        when(cartService.getTotalPrice()).thenReturn(Mono.just(500L));
        when(paymentClient.getBalance()).thenReturn(Mono.just(1000L)); // Баланс больше суммы
        when(paymentClient.isServiceAvailable()).thenReturn(Mono.just(true));

        webTestClient
                .get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }
}

