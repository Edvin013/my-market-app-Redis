package ru.mirakyan.mymarket.service;

import reactor.core.publisher.Mono;

/**
 * Клиент для взаимодействия с сервисом платежей
 */
public interface PaymentClient {

    /**
     * Получить баланс
     * @return баланс в копейках
     */
    Mono<Long> getBalance();

    /**
     * Обработать платеж
     * @param amount сумма платежа в копейках
     * @param orderId идентификатор заказа
     * @param description описание платежа
     * @return успешность платежа
     */
    Mono<Boolean> processPayment(Long amount, Long orderId, String description);

    /**
     * Проверить доступность сервиса платежей
     * @return true если сервис доступен
     */
    Mono<Boolean> isServiceAvailable();
}

