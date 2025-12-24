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
import ru.mirakyan.mymarket.dto.OrderDto;
import ru.mirakyan.mymarket.security.UserDetailsServiceImpl;
import ru.mirakyan.mymarket.service.OrderService;

import static org.mockito.Mockito.when;

/**
 * Простые тесты для OrderController
 */
@WebFluxTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void shouldDenyAccessToOrdersForAnonymousUser() {
        // Анонимные пользователи не могут просматривать заказы
        // formLogin делает редирект на страницу логина (302)
        webTestClient
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldAllowAccessToOrdersForAuthenticatedUser() {
        // Настраиваем мок
        when(orderService.getAllOrders())
                .thenReturn(Flux.empty());

        // Авторизованные пользователи могут просматривать свои заказы
        webTestClient
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldDenyAccessToSingleOrderForAnonymousUser() {
        // Анонимные пользователи не могут просматривать конкретный заказ
        // formLogin делает редирект на страницу логина (302)
        webTestClient
                .get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldAllowAccessToSingleOrderForAuthenticatedUser() {
        // Настраиваем мок
        OrderDto order = new OrderDto();
        order.setId(1L);
        order.setTotalSum(1000L);

        when(orderService.getOrderById(1L))
                .thenReturn(Mono.just(order));

        // Авторизованные пользователи могут просматривать свой заказ
        webTestClient
                .get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void shouldDisplayOrdersList() {
        // Создаем тестовые данные
        OrderDto order1 = new OrderDto();
        order1.setId(1L);
        order1.setTotalSum(1000L);

        OrderDto order2 = new OrderDto();
        order2.setId(2L);
        order2.setTotalSum(2000L);

        // Настраиваем мок
        when(orderService.getAllOrders())
                .thenReturn(Flux.just(order1, order2));

        // Проверяем отображение списка заказов
        webTestClient
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }
}

