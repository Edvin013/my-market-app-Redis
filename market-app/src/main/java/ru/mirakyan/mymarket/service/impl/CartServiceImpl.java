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
import ru.mirakyan.mymarket.service.UserService;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final ItemDtoMapper itemDtoMapper;
    private final UserService userService;

    @Override
    public Flux<ItemDto> getCartItems() {
        return userService.getCurrentUserId()
                .flatMapMany(this::getCartItemsForUser);
    }

    /**
     * Получает товары из корзины пользователя
     */
    private Flux<ItemDto> getCartItemsForUser(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .flatMap(this::enrichCartItemWithProduct);
    }

    /**
     * Обогащает CartItem информацией о товаре и преобразует в DTO
     */
    private Mono<ItemDto> enrichCartItemWithProduct(CartItem cartItem) {
        return itemRepository.findById(cartItem.getItemId())
                .map(item -> {
                    cartItem.setItem(item);
                    return itemDtoMapper.fromCartItem(cartItem);
                });
    }

    @Override
    public Mono<Long> getTotalPrice() {
        return userService.getCurrentUserId()
                .flatMapMany(this::calculateTotalPriceForUser)
                .reduce(0L, Long::sum);
    }

    /**
     * Вычисляет стоимость каждого товара в корзине пользователя
     */
    private Flux<Long> calculateTotalPriceForUser(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .flatMap(this::calculateCartItemPrice);
    }

    /**
     * Вычисляет стоимость одного товара в корзине
     */
    private Mono<Long> calculateCartItemPrice(CartItem cartItem) {
        return itemRepository.findById(cartItem.getItemId())
                .map(item -> item.getPrice() * cartItem.getCount());
    }

    @Override
    public Mono<Void> updateCartItem(Long itemId, ItemAction action) {
        return userService.getCurrentUserId()
                .flatMap(userId -> updateCartItemForUser(userId, itemId, action));
    }

    /**
     * Обновляет товар в корзине пользователя
     */
    private Mono<Void> updateCartItemForUser(Long userId, Long itemId, ItemAction action) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(itemId)))
                .flatMap(item -> performCartAction(userId, itemId, action));
    }

    /**
     * Выполняет действие с товаром в корзине
     */
    private Mono<Void> performCartAction(Long userId, Long itemId, ItemAction action) {
        Mono<CartItem> cartItemMono = cartItemRepository.findByUserIdAndItemId(userId, itemId);

        return switch (action) {
            case PLUS -> incrementCartItem(cartItemMono, userId, itemId);
            case MINUS -> decrementCartItem(cartItemMono);
            case DELETE -> deleteCartItem(cartItemMono);
            default -> Mono.empty();
        };
    }

    /**
     * Удаляет товар из корзины
     */
    private Mono<Void> deleteCartItem(Mono<CartItem> cartItemMono) {
        return cartItemMono
                .flatMap(cartItemRepository::delete)
                .then();
    }

    /**
     * Уменьшает количество товара в корзине или удаляет, если количество становится 0
     */
    private Mono<Void> decrementCartItem(Mono<CartItem> cartItemMono) {
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

    /**
     * Увеличивает количество товара в корзине или добавляет новый, если его еще нет
     */
    private Mono<Void> incrementCartItem(Mono<CartItem> cartItemMono, Long userId, Long itemId) {
        return cartItemMono
                .flatMap(cartItem -> {
                    cartItem.setCount(cartItem.getCount() + 1);
                    return cartItemRepository.save(cartItem);
                })
                .switchIfEmpty(Mono.defer(() ->
                        cartItemRepository.save(new CartItem(null, userId, itemId, null, 1))
                ))
                .then();
    }

    @Override
    public Mono<Void> clearCart() {
        return userService.getCurrentUserId()
                .flatMap(cartItemRepository::deleteByUserId);
    }
}

