package ru.mirakyan.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.OrderDto;
import ru.mirakyan.mymarket.service.OrderService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testGetOrders() {
        OrderDto order = new OrderDto(1L, List.of(), 1000L);

        when(orderService.getAllOrders()).thenReturn(Flux.just(order));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    byte[] responseBody = response.getResponseBody();
                    assert responseBody != null;
                    String body = new String(responseBody, StandardCharsets.UTF_8);
                    assert body.contains("orders");
                });

        verify(orderService).getAllOrders();
    }

    @Test
    void testGetOrder() {
        OrderDto orderDto = new OrderDto(1L, List.of(), 1000L);

        when(orderService.getOrderById(1L)).thenReturn(Mono.just(orderDto));

        webTestClient.get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    byte[] responseBody = response.getResponseBody();
                    assert responseBody != null;
                    String body = new String(responseBody, StandardCharsets.UTF_8);
                    assert body.contains("order");
                });

        verify(orderService).getOrderById(1L);
    }

    @Test
    void testCreateOrder() {
        when(orderService.createOrder()).thenReturn(Mono.just(1L));

        webTestClient.post()
                .uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/1?newOrder=true");

        verify(orderService).createOrder();
    }
}

