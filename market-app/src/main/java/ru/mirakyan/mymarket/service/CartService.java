package ru.mirakyan.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.enums.ItemAction;

public interface CartService {
    Flux<ItemDto> getCartItems();
    Mono<Long> getTotalPrice();
    Mono<Void> updateCartItem(Long itemId, ItemAction action);
    Mono<Void> clearCart();
}
