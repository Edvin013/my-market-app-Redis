package ru.mirakyan.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.exception.ItemNotFoundException;
import ru.mirakyan.mymarket.mapper.ItemDtoMapper;
import ru.mirakyan.mymarket.model.CartItem;
import ru.mirakyan.mymarket.repository.CartItemRepository;
import ru.mirakyan.mymarket.repository.ItemRepository;
import ru.mirakyan.mymarket.service.CartService;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;

    @Override
    public Flux<ItemDto> getCartItems() {
        return cartItemRepository.findAll()
                .flatMap(cartItem ->
                    itemRepository.findById(cartItem.getItemId())
                        .map(item -> {
                            cartItem.setItem(item);
                            return itemDtoMapper.fromCartItem(cartItem);
                        })
                );
    }

    @Override
    public Mono<Long> getTotalPrice() {
        return cartItemRepository.findAll()
                .flatMap(cartItem ->
                    itemRepository.findById(cartItem.getItemId())
                        .map(item -> item.getPrice() * cartItem.getCount())
                )
                .reduce(0L, Long::sum);
    }

    @Override
    public Mono<Void> updateCartItem(Long itemId, ItemAction action) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(itemId)))
                .flatMap(item -> {
                    Mono<CartItem> cartItemMono = cartItemRepository.findByItemId(itemId);

                    if (action == ItemAction.PLUS) {
                        return plus(cartItemMono, itemId);
                    } else if (action == ItemAction.MINUS) {
                        return minus(cartItemMono);
                    } else if (action == ItemAction.DELETE) {
                        return cartItemMono
                                .flatMap(cartItemRepository::delete)
                                .then();
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> minus(Mono<CartItem> cartItemMono) {
        return cartItemMono
                .flatMap(cartItem -> {
                    if (cartItem.getCount() > 1) {
                        cartItem.setCount(cartItem.getCount() - 1);
                        return cartItemRepository.save(cartItem).then();
                    } else {
                        return cartItemRepository.delete(cartItem);
                    }
                })
                .then();
    }

    private Mono<Void> plus(Mono<CartItem> cartItemMono, Long itemId) {
        return cartItemMono
                .flatMap(cartItem -> {
                    cartItem.setCount(cartItem.getCount() + 1);
                    return cartItemRepository.save(cartItem);
                })
                .switchIfEmpty(Mono.defer(() ->
                    cartItemRepository.save(new CartItem(null, itemId, null, 1))
                ))
                .then();
    }

    @Override
    public Mono<Void> clearCart() {
        return cartItemRepository.deleteAll();
    }
}

