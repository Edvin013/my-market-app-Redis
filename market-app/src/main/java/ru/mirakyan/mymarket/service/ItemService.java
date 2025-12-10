package ru.mirakyan.mymarket.service;

import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.dto.ItemDto;
import ru.mirakyan.mymarket.dto.PagingDto;
import ru.mirakyan.mymarket.enums.ItemAction;
import ru.mirakyan.mymarket.enums.SortType;

import java.util.List;

public interface ItemService {
    Mono<List<List<ItemDto>>> getItems(String search, SortType sort, int pageNumber, int pageSize);
    Mono<ItemDto> getItemById(Long id);
    Mono<Void> updateCartItem(Long itemId, ItemAction action);
    Mono<PagingDto> getPagingInfo(String search, int pageNumber, int pageSize);
}
