package ru.mirakyan.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.dto.PagingDto;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.enums.SortType;
import ru.mirakyan.mymarket.exception.ItemNotFoundException;
import ru.mirakyan.mymarket.mapper.ItemDtoMapper;
import ru.mirakyan.mymarket.model.Item;
import ru.mirakyan.mymarket.repository.CartItemRepository;
import ru.mirakyan.mymarket.repository.ItemRepository;
import ru.mirakyan.mymarket.service.CartService;
import ru.mirakyan.mymarket.service.ItemService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemDtoMapper itemDtoMapper;
    private final CartService cartService;

    @Override
    public Mono<List<List<ItemDto>>> getItems(String search, SortType sort, int pageNumber, int pageSize) {
        Flux<Item> itemsFlux = (!search.isEmpty())
                ? itemRepository.findByTitleOrDescriptionContaining(search)
                : itemRepository.findAll();

        return getCartCounts()
                .flatMap(cartCounts ->
                    itemsFlux
                        .sort(getComparator(sort))
                        .map(item -> itemDtoMapper.fromItem(item, cartCounts.getOrDefault(item.getId(), 0)))
                        .collectList()
                        .map(itemDtos -> {
                            int startIndex = Math.max(0, (pageNumber - 1) * pageSize);
                            if (startIndex >= itemDtos.size()) {
                                return Collections.<ItemDto>emptyList();
                            }
                            int endIndex = Math.min(startIndex + pageSize, itemDtos.size());
                            return itemDtos.subList(startIndex, endIndex);
                        })
                        .map(this::groupByThreeWithPlaceholders)
                );
    }

    @Override
    public Mono<ItemDto> getItemById(Long id) {
        return itemRepository.findById(id)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(id)))
                .flatMap(item ->
                    cartItemRepository.findByItemId(id)
                        .map(cartItem -> cartItem.getCount())
                        .defaultIfEmpty(0)
                        .map(count -> itemDtoMapper.fromItem(item, count))
                );
    }

    @Override
    public Mono<Void> updateCartItem(Long itemId, ItemAction action) {
        return itemRepository.findById(itemId)
                .switchIfEmpty(Mono.error(new ItemNotFoundException(itemId)))
                .flatMap(item -> cartService.updateCartItem(itemId, action));
    }

    @Override
    public Mono<PagingDto> getPagingInfo(String search, int pageNumber, int pageSize) {
        Mono<Long> totalItemsMono = (search != null && !search.isEmpty())
                ? itemRepository.findByTitleOrDescriptionContaining(search).count()
                : itemRepository.count();

        return totalItemsMono.map(totalItems -> {
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            boolean hasPrevious = pageNumber > 1;
            boolean hasNext = pageNumber < totalPages;
            return new PagingDto(pageSize, pageNumber, hasPrevious, hasNext);
        });
    }

    private Comparator<Item> getComparator(SortType sort) {
        if (sort == SortType.ALPHA) {
            return Comparator.comparing(Item::getTitle);
        } else if (sort == SortType.PRICE) {
            return Comparator.comparing(Item::getPrice);
        }
        return (a, b) -> 0;
    }

    private Mono<Map<Long, Integer>> getCartCounts() {
        return cartItemRepository.findAll()
                .collectMap(ci -> ci.getItemId(), ci -> ci.getCount());
    }

    private List<List<ItemDto>> groupByThreeWithPlaceholders(List<ItemDto> items) {
        List<List<ItemDto>> grouped = new ArrayList<>();
        for (int i = 0; i < items.size(); i += 3) {
            List<ItemDto> row = new ArrayList<>(3);
            for (int j = 0; j < 3; j++) {
                int idx = i + j;
                if (idx < items.size()) {
                    row.add(items.get(idx));
                } else {
                    row.add(new ItemDto(-1L, "", "", "", 0L, 0));
                }
            }
            grouped.add(row);
        }
        return grouped;
    }
}

