package ru.mirakyan.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.dto.OrderDto;
import ru.mirakyan.mymarket.mapper.ItemDtoMapper;
import ru.mirakyan.mymarket.model.CartItem;
import ru.mirakyan.mymarket.model.Item;
import ru.mirakyan.mymarket.model.Order;
import ru.mirakyan.mymarket.model.OrderItem;
import ru.mirakyan.mymarket.repository.CartItemRepository;
import ru.mirakyan.mymarket.repository.ItemRepository;
import ru.mirakyan.mymarket.repository.OrderRepository;
import ru.mirakyan.mymarket.repository.OrderItemRepository;
import ru.mirakyan.mymarket.service.impl.OrderServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @Spy
    private ItemDtoMapper itemDtoMapper = new ItemDtoMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private Item item1;
    private Item item2;
    private CartItem cartItem1;
    private CartItem cartItem2;
    private Order order;
    private OrderItem orderItem1;
    private OrderItem orderItem2;

    @BeforeEach
    void setUp() {
        item1 = new Item(1L, "Ball", "Red ball", "/images/ball.jpg", 100L);
        item2 = new Item(2L, "Apple", "Fresh apple", "/images/apple.jpg", 50L);

        cartItem1 = new CartItem(1L, 1L, item1, 2);
        cartItem2 = new CartItem(2L, 2L, item2, 3);

        order = new Order(350L);
        order.setId(1L);

        orderItem1 = new OrderItem(1L, 1L, 1L, item1, 2, 100L);
        orderItem2 = new OrderItem(2L, 1L, 2L, item2, 3, 50L);
    }

    @Test
    void createOrder_shouldCreateOrderFromCart() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(Mono.just(orderItem1))
                .thenReturn(Mono.just(orderItem2));
        when(cartService.clearCart()).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder())
                .expectNext(1L)
                .verifyComplete();

        verify(orderRepository).save(any(Order.class));
        verify(cartService).clearCart();
    }

    @Test
    void createOrder_shouldThrowException_whenCartIsEmpty() {
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(orderService.createOrder())
                .expectError()
                .verify();

        verify(orderRepository, never()).save(any(Order.class));
        verify(cartService, never()).clearCart();
    }

    @Test
    void createOrder_shouldCalculateCorrectTotal() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return Mono.just(savedOrder);
        });
        when(orderItemRepository.save(any(OrderItem.class)))
                .thenReturn(Mono.just(orderItem1))
                .thenReturn(Mono.just(orderItem2));
        when(cartService.clearCart()).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrder())
                .expectNext(1L)
                .verifyComplete();

        verify(orderRepository).save(argThat(o -> o.getTotalSum() == 350L));
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        when(orderRepository.findAll()).thenReturn(Flux.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem1, orderItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));

        StepVerifier.create(orderService.getAllOrders())
                .assertNext(orderDto -> {
                    assert orderDto.getId() == 1L;
                    assert orderDto.getTotalSum() == 350L;
                    assert orderDto.getItems().size() == 2;
                })
                .verifyComplete();
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem1, orderItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));

        StepVerifier.create(orderService.getOrderById(1L))
                .assertNext(orderDto -> {
                    assert orderDto.getId() == 1L;
                    assert orderDto.getTotalSum() == 350L;
                    assert orderDto.getItems().size() == 2;
                })
                .verifyComplete();
    }

    @Test
    void getOrderById_shouldThrowException_whenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrderById(99L))
                .expectError()
                .verify();
    }

    @Test
    void getOrderById_shouldReturnCorrectItemDtos() {
        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem1, orderItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));

        StepVerifier.create(orderService.getOrderById(1L))
                .assertNext(orderDto -> {
                    List<ItemDto> items = orderDto.getItems();
                    assert items.size() == 2;

                    ItemDto itemDto1 = items.get(0);
                    assert itemDto1.getId() == 1L;
                    assert itemDto1.getTitle().equals("Ball");
                    assert itemDto1.getPrice() == 100L;
                    assert itemDto1.getCount() == 2;

                    ItemDto itemDto2 = items.get(1);
                    assert itemDto2.getId() == 2L;
                    assert itemDto2.getTitle().equals("Apple");
                    assert itemDto2.getPrice() == 50L;
                    assert itemDto2.getCount() == 3;
                })
                .verifyComplete();
    }
}
