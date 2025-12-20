package ru.mirakyan.mymarket.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.security.SecurityUtils;
import ru.mirakyan.mymarket.service.PaymentClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentClientImpl implements PaymentClient {

    private final WebClient paymentWebClient;

    @Override
    public Mono<Long> getBalance() {
        log.debug("Запрос баланса из сервиса платежей");

        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> 
                    paymentWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/api/v1/payments/balance")
                                .queryParam("username", username)
                                .build())
                        .retrieve()
                        .bodyToMono(BalanceResponse.class)
                        .map(BalanceResponse::getBalance)
                        .doOnSuccess(balance -> log.info("Баланс получен для {}: {}", username, balance))
                        .doOnError(error -> log.error("Ошибка при получении баланса из сервиса платежей", error))
                        .onErrorResume(error -> {
                            log.warn("Сервис платежей недоступен, возвращается баланс 0");
                            return Mono.just(0L);
                        })
                );
    }

    @Override
    public Mono<Boolean> processPayment(Long amount, Long orderId, String description) {
        log.debug("Обработка платежа: сумма={}, orderId={}", amount, orderId);

        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> {
                    Map<String, Object> request = new HashMap<>();
                    request.put("username", username);
                    request.put("amount", amount);
                    request.put("orderId", orderId);
                    request.put("description", description);

                    return paymentWebClient.post()
                            .uri("/api/v1/payments/process")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(PaymentResponse.class)
                            .map(PaymentResponse::isSuccess)
                            .doOnSuccess(success -> {
                                if (success) {
                                    log.info("Платеж успешно обработан для {}: orderId={}", username, orderId);
                                } else {
                                    log.warn("Платеж не выполнен для {}: orderId={}", username, orderId);
                                }
                            })
                            .doOnError(error -> log.error("Ошибка при обработке платежа: orderId={}", orderId, error))
                            .onErrorResume(WebClientResponseException.class, error -> {
                                log.error("Сервис платежей вернул ошибку: статус={}, тело={}",
                                         error.getStatusCode(), error.getResponseBodyAsString());
                                return Mono.just(false);
                            })
                            .onErrorReturn(false);
                });
    }

    @Override
    public Mono<Boolean> isServiceAvailable() {
        log.debug("Проверка доступности сервиса платежей");

        return paymentWebClient.get()
                .uri("/api/v1/payments/balance")
                .retrieve()
                .toBodilessEntity()
                .map(response -> response.getStatusCode().is2xxSuccessful())
                .doOnSuccess(available -> log.debug("Сервис платежей доступен: {}", available))
                .onErrorReturn(false);
    }

    // DTO классы для десериализации ответов
    private static class BalanceResponse {
        private Long balance;

        public Long getBalance() {
            return balance;
        }

        public void setBalance(Long balance) {
            this.balance = balance;
        }
    }

    private static class PaymentResponse {
        private Boolean success;
        private String transactionId;
        private Long remainingBalance;
        private String message;

        public Boolean isSuccess() {
            return success;
        }

        public void setSuccess(Boolean success) {
            this.success = success;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public Long getRemainingBalance() {
            return remainingBalance;
        }

        public void setRemainingBalance(Long remainingBalance) {
            this.remainingBalance = remainingBalance;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
