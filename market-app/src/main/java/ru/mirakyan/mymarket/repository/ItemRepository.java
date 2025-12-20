package ru.mirakyan.mymarket.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.mirakyan.mymarket.model.Item;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<Item, Long> {
    @Query("SELECT * FROM items WHERE LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Flux<Item> findByTitleOrDescriptionContaining(String search);
}

