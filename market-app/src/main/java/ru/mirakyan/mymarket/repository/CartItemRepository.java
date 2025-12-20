package ru.mirakyan.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.model.CartItem;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {
    Flux<CartItem> findByUserId(Long userId);
    
    Mono<CartItem> findByUserIdAndItemId(Long userId, Long itemId);
    
    Mono<Void> deleteByUserId(Long userId);
}

