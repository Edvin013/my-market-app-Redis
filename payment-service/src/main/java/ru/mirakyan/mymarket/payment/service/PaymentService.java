package ru.mirakyan.mymarket.payment.service;

import reactor.core.publisher.Mono;

public interface PaymentService {
    Mono<Long> getBalance();

    Mono<PaymentResult> processPayment(Long amount, Long orderId, String description);
}

