package ru.mirakyan.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.model.Item;
import ru.mirakyan.mymarket.service.ItemCacheService;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemCacheServiceImpl implements ItemCacheService {

    private static final String ITEM_KEY_PREFIX = "item:";
    private static final String ALL_ITEMS_KEY = "items:all";

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Value("${cache.item.ttl:120}")
    private long cacheTtlSeconds;

    @Override
    public Mono<Item> getItemFromCache(Long id) {
        String key = ITEM_KEY_PREFIX + id;
        log.debug("Получение товара из кэша: ключ={}", key);

        return redisTemplate.opsForValue()
                .get(key)
                .cast(Item.class)
                .doOnSuccess(item -> {
                    if (item != null) {
                        log.debug("Кэш HIT для товара: id={}", id);
                    } else {
                        log.debug("Кэш MISS для товара: id={}", id);
                    }
                })
                .doOnError(error -> log.error("Ошибка при получении товара из кэша: id={}", id, error));
    }

    @Override
    public Mono<Item> saveItemToCache(Item item) {
        String key = ITEM_KEY_PREFIX + item.getId();
        log.debug("Сохранение товара в кэш: ключ={}, ttl={}с", key, cacheTtlSeconds);

        return redisTemplate.opsForValue()
                .set(key, item, Duration.ofSeconds(cacheTtlSeconds))
                .thenReturn(item)
                .doOnSuccess(savedItem -> log.debug("Товар сохранен в кэш: id={}", item.getId()))
                .doOnError(error -> log.error("Ошибка при сохранении товара в кэш: id={}", item.getId(), error));
    }

    @Override
    public Flux<Item> getAllItemsFromCache() {
        log.debug("Получение всех товаров из кэша");

        return redisTemplate.opsForList()
                .range(ALL_ITEMS_KEY, 0, -1)
                .cast(Item.class)
                .doOnComplete(() -> log.debug("Все товары получены из кэша"))
                .doOnError(error -> log.error("Ошибка при получении всех товаров из кэша", error));
    }

    @Override
    public Mono<Long> saveAllItemsToCache(List<Item> items) {
        log.debug("Сохранение {} товаров в кэш", items.size());

        return redisTemplate.delete(ALL_ITEMS_KEY)
                .then(Mono.defer(() -> {
                    if (items.isEmpty()) {
                        return Mono.just(0L);
                    }
                    return redisTemplate.opsForList()
                            .rightPushAll(ALL_ITEMS_KEY, items.toArray())
                            .flatMap(count -> redisTemplate.expire(ALL_ITEMS_KEY, Duration.ofSeconds(cacheTtlSeconds))
                                    .thenReturn(count));
                }))
                .doOnSuccess(count -> log.debug("Сохранено {} товаров в кэш", count))
                .doOnError(error -> log.error("Ошибка при сохранении всех товаров в кэш", error));
    }

    @Override
    public Mono<Boolean> deleteItemFromCache(Long id) {
        String key = ITEM_KEY_PREFIX + id;
        log.debug("Удаление товара из кэша: ключ={}", key);

        return redisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnSuccess(deleted -> {
                    if (deleted) {
                        log.debug("Товар удален из кэша: id={}", id);
                    } else {
                        log.debug("Товар не найден в кэше: id={}", id);
                    }
                })
                .doOnError(error -> log.error("Ошибка при удалении товара из кэша: id={}", id, error));
    }

    @Override
    public Mono<Void> clearCache() {
        log.debug("Очистка кэша всех товаров");

        return redisTemplate.keys(ITEM_KEY_PREFIX + "*")
                .flatMap(redisTemplate::delete)
                .then(redisTemplate.delete(ALL_ITEMS_KEY))
                .then()
                .doOnSuccess(v -> log.info("Кэш успешно очищен"))
                .doOnError(error -> log.error("Ошибка при очистке кэша", error));
    }
}
