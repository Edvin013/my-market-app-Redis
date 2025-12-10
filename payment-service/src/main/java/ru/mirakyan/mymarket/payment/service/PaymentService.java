package ru.mirakyan.mymarket.payment.service;

import reactor.core.publisher.Mono;

public interface PaymentService {
    /**
     * Получить текущий баланс
     * @return баланс в копейках
     */
    Mono<Long> getBalance();

    /**
     * Обработать платеж
     * @param amount сумма платежа в копейках
     * @param orderId идентификатор заказа
     * @param description описание платежа
     * @return результат обработки платежа
     */
    Mono<PaymentResult> processPayment(Long amount, Long orderId, String description);
}

