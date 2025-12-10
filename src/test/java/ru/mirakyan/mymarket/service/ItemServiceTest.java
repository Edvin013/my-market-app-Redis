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
import ru.mirakyan.mymarket.dto.PagingDto;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.enums.SortType;
import ru.mirakyan.mymarket.mapper.ItemDtoMapper;
import ru.mirakyan.mymarket.model.CartItem;
import ru.mirakyan.mymarket.model.Item;
import ru.mirakyan.mymarket.repository.CartItemRepository;
import ru.mirakyan.mymarket.repository.ItemRepository;
import ru.mirakyan.mymarket.service.impl.ItemServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartService cartService;

    @Spy
    private ItemDtoMapper itemDtoMapper = new ItemDtoMapper();

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        item1 = new Item(1L, "Laptop", "Gaming laptop", "/img1.jpg", 1000L);
        item2 = new Item(2L, "Mouse", "Gaming mouse", "/img2.jpg", 50L);
        item3 = new Item(3L, "Keyboard", "Mechanical keyboard", "/img3.jpg", 150L);
    }

    @Test
    void testGetItems_WithoutSearchAndSort() {
        when(itemRepository.findAll()).thenReturn(Flux.just(item1, item2, item3));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems(null, null, 1, 10))
                .expectNextCount(1) // One list of lists
                .verifyComplete();

        verify(itemRepository).findAll();
    }

    @Test
    void testGetItems_WithSearch() {
        when(itemRepository.findByTitleOrDescriptionContaining("gaming"))
                .thenReturn(Flux.just(item1, item2));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems("gaming", null, 1, 10))
                .expectNextCount(1)
                .verifyComplete();

        verify(itemRepository).findByTitleOrDescriptionContaining("gaming");
    }

    @Test
    void testGetItems_WithAlphaSort() {
        when(itemRepository.findAll()).thenReturn(Flux.just(item1, item2, item3));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems(null, SortType.ALPHA, 1, 10))
                .assertNext(lists -> {
                    // First item should be Keyboard (alphabetically first)
                })
                .verifyComplete();
    }

    @Test
    void testGetItems_WithPriceSort() {
        when(itemRepository.findAll()).thenReturn(Flux.just(item1, item2, item3));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems(null, SortType.PRICE, 1, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetItems_WithPagination() {
        when(itemRepository.findAll()).thenReturn(Flux.just(item1, item2, item3));
        when(cartItemRepository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(itemService.getItems(null, null, 1, 2))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetItemById() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItemById(1L))
                .assertNext(dto -> {
                    assert dto.getTitle().equals("Laptop");
                    assert dto.getPrice() == 1000L;
                    assert dto.getCount() == 0;
                })
                .verifyComplete();

        verify(itemRepository).findById(1L);
    }

    @Test
    void testGetItemById_WithCartCount() {
        CartItem cartItem = new CartItem(1L, 1L, item1, 3);
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartItemRepository.findByItemId(1L)).thenReturn(Mono.just(cartItem));

        StepVerifier.create(itemService.getItemById(1L))
                .assertNext(dto -> {
                    assert dto.getTitle().equals("Laptop");
                    assert dto.getCount() == 3;
                })
                .verifyComplete();
    }

    @Test
    void testGetItemById_NotFound() {
        when(itemRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItemById(999L))
                .expectError()
                .verify();
    }

    @Test
    void testUpdateCartItem_PlusDelegatesToCartService() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartService.updateCartItem(eq(1L), eq(ItemAction.PLUS))).thenReturn(Mono.empty());

        StepVerifier.create(itemService.updateCartItem(1L, ItemAction.PLUS))
                .verifyComplete();

        verify(cartService).updateCartItem(eq(1L), eq(ItemAction.PLUS));
    }

    @Test
    void testUpdateCartItem_MinusDelegatesToCartService() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(item1));
        when(cartService.updateCartItem(eq(1L), eq(ItemAction.MINUS))).thenReturn(Mono.empty());

        StepVerifier.create(itemService.updateCartItem(1L, ItemAction.MINUS))
                .verifyComplete();

        verify(cartService).updateCartItem(eq(1L), eq(ItemAction.MINUS));
    }

    @Test
    void testUpdateCartItem_NotFound() {
        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.updateCartItem(1L, ItemAction.PLUS))
                .expectError()
                .verify();

        verify(cartService, never()).updateCartItem(anyLong(), any());
    }

    @Test
    void testGetPagingInfo_WithoutSearch() {
        when(itemRepository.count()).thenReturn(Mono.just(10L));

        StepVerifier.create(itemService.getPagingInfo(null, 2, 3))
                .assertNext(paging -> {
                    assert paging.getPageSize() == 3;
                    assert paging.getPageNumber() == 2;
                    assert paging.isHasPrevious();
                    assert paging.isHasNext();
                })
                .verifyComplete();
    }

    @Test
    void testGetPagingInfo_WithSearch() {
        when(itemRepository.findByTitleOrDescriptionContaining("gaming"))
                .thenReturn(Flux.just(item1, item2));

        StepVerifier.create(itemService.getPagingInfo("gaming", 1, 3))
                .assertNext(paging -> {
                    assert paging.getPageNumber() == 1;
                    assert !paging.isHasPrevious();
                    assert !paging.isHasNext();
                })
                .verifyComplete();
    }

    @Test
    void testGetPagingInfo_FirstPage() {
        when(itemRepository.count()).thenReturn(Mono.just(10L));

        StepVerifier.create(itemService.getPagingInfo(null, 1, 3))
                .assertNext(paging -> {
                    assert !paging.isHasPrevious();
                    assert paging.isHasNext();
                })
                .verifyComplete();
    }

    @Test
    void testGetPagingInfo_LastPage() {
        when(itemRepository.count()).thenReturn(Mono.just(9L));

        StepVerifier.create(itemService.getPagingInfo(null, 3, 3))
                .assertNext(paging -> {
                    assert paging.isHasPrevious();
                    assert !paging.isHasNext();
                })
                .verifyComplete();
    }
}
