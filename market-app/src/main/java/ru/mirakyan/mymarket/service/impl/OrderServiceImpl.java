package ru.mirakyan.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.OrderDto;
import ru.mirakyan.mymarket.exception.EmptyCartException;
import ru.mirakyan.mymarket.exception.OrderNotFoundException;
import ru.mirakyan.mymarket.mapper.ItemDtoMapper;
import ru.mirakyan.mymarket.model.Order;
import ru.mirakyan.mymarket.model.OrderItem;
import ru.mirakyan.mymarket.repository.CartItemRepository;
import ru.mirakyan.mymarket.repository.ItemRepository;
import ru.mirakyan.mymarket.repository.OrderItemRepository;
import ru.mirakyan.mymarket.repository.OrderRepository;
import ru.mirakyan.mymarket.service.CartService;
import ru.mirakyan.mymarket.service.OrderService;
import ru.mirakyan.mymarket.service.PaymentClient;
import ru.mirakyan.mymarket.service.UserService;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;
    private final CartService cartService;
    private final ItemDtoMapper itemDtoMapper;
    private final PaymentClient paymentClient;
    private final UserService userService;

    @Override
    public Mono<Long> createOrder() {
        return userService.getCurrentUserId()
                .flatMap(this::createOrderForUser);
    }

    /**
     * Создает заказ для пользователя
     */
    private Mono<Long> createOrderForUser(Long userId) {
        return cartItemRepository.findByUserId(userId)
                .collectList()
                .flatMap(cartItems -> validateAndProcessOrder(userId, cartItems));
    }

    /**
     * Валидирует корзину и обрабатывает заказ
     */
    private Mono<Long> validateAndProcessOrder(Long userId, java.util.List<ru.mirakyan.mymarket.model.CartItem> cartItems) {
        if (cartItems.isEmpty()) {
            return Mono.error(new EmptyCartException());
        }

        return calculateTotalPrice(cartItems)
                .flatMap(totalSum -> processPaymentAndCreateOrder(userId, cartItems, totalSum));
    }

    /**
     * Вычисляет общую стоимость заказа
     */
    private Mono<Long> calculateTotalPrice(java.util.List<ru.mirakyan.mymarket.model.CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem ->
                        itemRepository.findById(cartItem.getItemId())
                                .map(item -> item.getPrice() * cartItem.getCount())
                )
                .reduce(0L, Long::sum);
    }

    /**
     * Обрабатывает платеж и создает заказ
     */
    private Mono<Long> processPaymentAndCreateOrder(Long userId, 
                                                     java.util.List<ru.mirakyan.mymarket.model.CartItem> cartItems, 
                                                     Long totalSum) {
        return paymentClient.processPayment(totalSum, null, "Оплата заказа")
                .flatMap(paymentSuccess -> {
                    if (!paymentSuccess) {
                        return Mono.error(new RuntimeException(
                                "Не удалось обработать платеж. Недостаточно средств или сервис платежей недоступен."));
                    }
                    return createOrderWithItems(userId, cartItems, totalSum);
                });
    }

    /**
     * Создает заказ и добавляет в него товары
     */
    private Mono<Long> createOrderWithItems(Long userId, 
                                            java.util.List<ru.mirakyan.mymarket.model.CartItem> cartItems, 
                                            Long totalSum) {
        Order order = new Order(userId, totalSum);
        return orderRepository.save(order)
                .flatMap(savedOrder -> saveOrderItemsAndClearCart(savedOrder, cartItems));
    }

    /**
     * Сохраняет товары заказа и очищает корзину
     */
    private Mono<Long> saveOrderItemsAndClearCart(Order savedOrder, 
                                                   java.util.List<ru.mirakyan.mymarket.model.CartItem> cartItems) {
        return Flux.fromIterable(cartItems)
                .flatMap(cartItem -> createOrderItem(savedOrder.getId(), cartItem))
                .flatMap(orderItemRepository::save)
                .then(cartService.clearCart())
                .thenReturn(savedOrder.getId());
    }

    /**
     * Создает OrderItem из CartItem
     */
    private Mono<OrderItem> createOrderItem(Long orderId, ru.mirakyan.mymarket.model.CartItem cartItem) {
        return itemRepository.findById(cartItem.getItemId())
                .map(item -> new OrderItem(
                        null,
                        orderId,
                        item.getId(),
                        null,
                        cartItem.getCount(),
                        item.getPrice()
                ));
    }

    @Override
    public Flux<OrderDto> getAllOrders() {
        return userService.getCurrentUserId()
                .flatMapMany(userId -> orderRepository.findByUserId(userId)
                        .flatMap(this::convertToDto)
                );
    }

    @Override
    public Mono<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
                .flatMap(this::convertToDto);
    }

    private Mono<OrderDto> convertToDto(Order order) {
        return orderItemRepository.findByOrderId(order.getId())
                .flatMap(orderItem ->
                        itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    orderItem.setItem(item);
                                    return itemDtoMapper.fromOrderItem(orderItem);
                                })
                )
                .collectList()
                .map(itemDtos -> new OrderDto(order.getId(), itemDtos, order.getTotalSum()));
    }
}

