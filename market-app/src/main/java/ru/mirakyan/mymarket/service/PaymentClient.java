package ru.mirakyan.mymarket.service;

import reactor.core.publisher.Mono;

public interface PaymentClient {
    Mono<Long> getBalance();
    Mono<Boolean> processPayment(Long amount, Long orderId, String description);
    Mono<Boolean> isServiceAvailable();
}

