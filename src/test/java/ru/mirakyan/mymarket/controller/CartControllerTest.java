package ru.mirakyan.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.service.CartService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @Test
    void testGetCartItems() {
        ItemDto item = new ItemDto(1L, "Test", "Desc", "/img", 100L, 2);

        when(cartService.getCartItems()).thenReturn(Flux.just(item));
        when(cartService.getTotalPrice()).thenReturn(Mono.just(200L));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String body = new String(response.getResponseBody());
                    assert body.contains("Test");
                });

        verify(cartService).getCartItems();
        verify(cartService).getTotalPrice();
    }

    @Test
    void testUpdateCartItem() {
        ItemDto item = new ItemDto(1L, "Test", "Desc", "/img", 100L, 2);

        when(cartService.getCartItems()).thenReturn(Flux.just(item));
        when(cartService.getTotalPrice()).thenReturn(Mono.just(200L));
        when(cartService.updateCartItem(anyLong(), any(ItemAction.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus().isOk();

        verify(cartService, times(1)).updateCartItem(1L, ItemAction.PLUS);
    }
}

