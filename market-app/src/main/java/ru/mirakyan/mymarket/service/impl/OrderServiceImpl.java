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

    @Override
    public Mono<Long> createOrder() {
        return cartItemRepository.findAll()
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.error(new EmptyCartException());
                    }

                    return Flux.fromIterable(cartItems)
                            .flatMap(cartItem ->
                                    itemRepository.findById(cartItem.getItemId())
                                            .map(item -> item.getPrice() * cartItem.getCount())
                            )
                            .reduce(0L, Long::sum)
                            .flatMap(totalSum -> {
                                return paymentClient.processPayment(totalSum, null, "Оплата заказа")
                                        .flatMap(paymentSuccess -> {
                                            if (!paymentSuccess) {
                                                return Mono.error(new RuntimeException("Не удалось обработать платеж. Недостаточно средств или сервис платежей недоступен."));
                                            }

                                            Order order = new Order(totalSum);
                                            return orderRepository.save(order)
                                                    .flatMap(savedOrder ->
                                                            Flux.fromIterable(cartItems)
                                                                    .flatMap(cartItem ->
                                                                            itemRepository.findById(cartItem.getItemId())
                                                                                    .map(item -> new OrderItem(
                                                                                            null,
                                                                                            savedOrder.getId(),
                                                                                            item.getId(),
                                                                                            null,
                                                                                            cartItem.getCount(),
                                                                                            item.getPrice()
                                                                                    ))
                                                                    )
                                                                    .flatMap(orderItemRepository::save)
                                                                    .then(cartService.clearCart())
                                                                    .thenReturn(savedOrder.getId())
                                                    );
                                        });
                            });
                });
    }

    @Override
    public Flux<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .flatMap(this::convertToDto);
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

