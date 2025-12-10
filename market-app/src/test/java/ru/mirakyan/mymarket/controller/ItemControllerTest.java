package ru.mirakyan.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.dto.PagingDto;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.enums.SortType;
import ru.mirakyan.mymarket.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ItemService itemService;

    @Test
    void testGetItems() {
        ItemDto item = new ItemDto(1L, "Test", "Desc", "/img", 100L, 0);
        List<List<ItemDto>> items = List.of(List.of(item));
        PagingDto paging = new PagingDto(5, 1, false, false);

        when(itemService.getItems(anyString(), any(SortType.class), anyInt(), anyInt())).thenReturn(Mono.just(items));
        when(itemService.getPagingInfo(anyString(), anyInt(), anyInt())).thenReturn(Mono.just(paging));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    byte[] responseBody = response.getResponseBody();
                    assert responseBody != null;
                    String body = new String(responseBody, StandardCharsets.UTF_8);
                    assert body.contains("Test");
                });

        verify(itemService).getItems(anyString(), any(), anyInt(), anyInt());
        verify(itemService).getPagingInfo(anyString(), anyInt(), anyInt());
    }

    @Test
    void testGetItemById() {
        ItemDto item = new ItemDto(1L, "Test", "Desc", "/img", 100L, 0);

        when(itemService.getItemById(1L)).thenReturn(Mono.just(item));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    byte[] responseBody = response.getResponseBody();
                    assert responseBody != null;
                    String body = new String(responseBody, StandardCharsets.UTF_8);
                    assert body.contains("Test");
                });

        verify(itemService).getItemById(1L);
    }

    @Test
    void testUpdateItemInCartFromItemsPage() {
        when(itemService.updateCartItem(anyLong(), any(ItemAction.class))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus().is3xxRedirection();

        verify(itemService, times(1)).updateCartItem(1L, ItemAction.PLUS);
    }

    @Test
    void testUpdateItemInCartFromItemPage() {
        ItemDto item = new ItemDto(1L, "Test", "Desc", "/img", 100L, 1);

        when(itemService.updateCartItem(anyLong(), any(ItemAction.class))).thenReturn(Mono.empty());
        when(itemService.getItemById(1L)).thenReturn(Mono.just(item));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/items/1")
                        .queryParam("action", "PLUS")
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .exchange()
                .expectStatus().isOk();

        verify(itemService, times(1)).updateCartItem(1L, ItemAction.PLUS);
    }
}

