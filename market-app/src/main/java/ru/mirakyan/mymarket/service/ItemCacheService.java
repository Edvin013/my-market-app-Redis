package ru.mirakyan.mymarket.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.model.Item;

import java.util.List;

/**
 * Сервис для кеширования товаров в Redis
 */
public interface ItemCacheService {

    /**
     * Получить товар из кеша
     * @param id идентификатор товара
     * @return товар или пустой Mono, если товар не найден в кеше
     */
    Mono<Item> getItemFromCache(Long id);

    /**
     * Сохранить товар в кеш
     * @param item товар
     * @return сохраненный товар
     */
    Mono<Item> saveItemToCache(Item item);

    /**
     * Получить список товаров из кеша
     * @return список товаров
     */
    Flux<Item> getAllItemsFromCache();

    /**
     * Сохранить список товаров в кеш
     * @param items список товаров
     * @return количество сохраненных товаров
     */
    Mono<Long> saveAllItemsToCache(List<Item> items);

    /**
     * Удалить товар из кеша
     * @param id идентификатор товара
     * @return успешность удаления
     */
    Mono<Boolean> deleteItemFromCache(Long id);

    /**
     * Очистить весь кеш товаров
     * @return успешность очистки
     */
    Mono<Void> clearCache();
}

