package ru.mirakyan.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.model.Item;

import java.util.List;


public interface ItemCacheService {
    Mono<Item> getItemFromCache(Long id);
    Mono<Item> saveItemToCache(Item item);
    Flux<Item> getAllItemsFromCache();
    Mono<Long> saveAllItemsToCache(List<Item> items);
    Mono<Boolean> deleteItemFromCache(Long id);
    Mono<Void> clearCache();
}

