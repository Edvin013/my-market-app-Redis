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
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.mapper.ItemDtoMapper;
import ru.mirakyan.mymarket.model.CartItem;
import ru.mirakyan.mymarket.model.Item;
import ru.mirakyan.mymarket.repository.CartItemRepository;
import ru.mirakyan.mymarket.repository.ItemRepository;
import ru.mirakyan.mymarket.service.impl.CartServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private ItemDtoMapper itemDtoMapper = new ItemDtoMapper();

    @InjectMocks
    private CartServiceImpl cartService;

    private Item item1;
    private Item item2;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        item1 = new Item(1L, "Ball", "Red ball", "/images/ball.jpg", 100L);
        item2 = new Item(2L, "Book", "Interesting book", "/images/book.jpg", 200L);
        cartItem1 = new CartItem(1L, 1L, item1, 2);
        cartItem2 = new CartItem(2L, 2L, item2, 1);
    }

    @Test
    void getCartItems_shouldReturnEmptyList_whenCartIsEmpty() {
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(cartService.getCartItems())
                .expectNextCount(0)
                .verifyComplete();

        verify(cartItemRepository).findAll();
    }

    @Test
    void getCartItems_shouldReturnCartItems() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));

        StepVerifier.create(cartService.getCartItems())
                .expectNextMatches(dto -> dto.getTitle().equals("Ball") && dto.getCount() == 2)
                .expectNextMatches(dto -> dto.getTitle().equals("Book") && dto.getCount() == 1)
                .verifyComplete();
    }

    @Test
    void getTotalPrice_shouldReturnZero_whenCartIsEmpty() {
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(cartService.getTotalPrice())
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void getTotalPrice_shouldCalculateCorrectTotal() {
        when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItem1, cartItem2));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(itemRepository.findById(2L)).thenReturn(Mono.just(item2));

        StepVerifier.create(cartService.getTotalPrice())
                .expectNext(400L) // 2*100 + 1*200
                .verifyComplete();
    }

    @Test
    void updateCartItem_shouldAddNewItem_whenActionIsPlus() {
        CartItem newCartItem = new CartItem(1L, 1);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(newCartItem));

        StepVerifier.create(cartService.updateCartItem(1L, ItemAction.PLUS))
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_shouldIncreaseCount_whenItemExists() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(cartItem1));

        StepVerifier.create(cartService.updateCartItem(1L, ItemAction.PLUS))
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_shouldDecreaseCount_whenActionIsMinus() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(cartItem1));

        StepVerifier.create(cartService.updateCartItem(1L, ItemAction.MINUS))
                .verifyComplete();

        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void updateCartItem_shouldDeleteItem_whenActionIsDelete() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem1));
        when(cartItemRepository.delete(any(CartItem.class))).thenReturn(Mono.empty());

        StepVerifier.create(cartService.updateCartItem(1L, ItemAction.DELETE))
                .verifyComplete();

        verify(cartItemRepository).delete(any(CartItem.class));
    }

    @Test
    void clearCart_shouldDeleteAllItems() {
        when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

        StepVerifier.create(cartService.clearCart())
                .verifyComplete();

        verify(cartItemRepository).deleteAll();
    }
}
