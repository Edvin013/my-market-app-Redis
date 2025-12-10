package ru.mirakyan.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.mirakyan.mymarket.model.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}

