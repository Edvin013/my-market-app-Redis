package ru.mirakyan.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.model.CartItem;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    Mono<CartItem> findByItemId(Long itemId);
}

