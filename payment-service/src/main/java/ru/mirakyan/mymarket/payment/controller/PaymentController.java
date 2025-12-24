package ru.mirakyan.mymarket.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.payment.security.SecurityUtils;
import ru.mirakyan.mymarket.payment.service.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/balance")
    public Mono<ResponseEntity<BalanceResponse>> getBalance() {
        log.debug("REST: Получение баланса из JWT токена");

        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> {
                    log.debug("REST: Запрос баланса для пользователя: {}", username);
                    return paymentService.getBalance(username)
                            .map(balance -> {
                                BalanceResponse response = new BalanceResponse();
                                response.setBalance(balance);
                                log.info("REST: Баланс получен для {}: {}", username, balance);
                                return ResponseEntity.ok(response);
                            });
                })
                .onErrorResume(error -> {
                    log.error("REST: Ошибка при получении баланса", error);
                    return Mono.just(ResponseEntity.status(401).build());
                });
    }

    @PostMapping("/process")
    public Mono<ResponseEntity<PaymentResponse>> processPayment(@RequestBody PaymentRequest paymentRequest) {
        log.debug("REST: Обработка платежа из JWT токена");

        return SecurityUtils.getCurrentUsername()
                .flatMap(username -> {
                    log.debug("REST: Запрос на платеж для пользователя {}: сумма={}, orderId={}",
                             username, paymentRequest.getAmount(), paymentRequest.getOrderId());

                    return paymentService.processPayment(
                            username,
                            paymentRequest.getAmount(),
                            paymentRequest.getOrderId(),
                            paymentRequest.getDescription()
                    )
                    .map(result -> {
                        PaymentResponse response = new PaymentResponse();
                        response.setSuccess(result.isSuccess());
                        response.setTransactionId(result.getTransactionId());
                        response.setRemainingBalance(result.getRemainingBalance());
                        response.setMessage(result.getMessage());

                        if (result.isSuccess()) {
                            log.info("REST: Платеж успешно обработан для {}: {}", username, result.getTransactionId());
                            return ResponseEntity.ok(response);
                        } else {
                            log.warn("REST: Платеж не выполнен для {}: {}", username, result.getMessage());
                            return ResponseEntity.badRequest().body(response);
                        }
                    });
                })
                .onErrorResume(error -> {
                    log.error("REST: Ошибка при обработке платежа", error);
                    PaymentResponse errorResponse = new PaymentResponse();
                    errorResponse.setSuccess(false);
                    errorResponse.setMessage("Ошибка аутентификации или обработки платежа");
                    return Mono.just(ResponseEntity.status(401).body(errorResponse));
                });
    }

    // DTO классы для десериализации ответов
    public static class BalanceResponse {
        private Long balance;

        public Long getBalance() {
            return balance;
        }

        public void setBalance(Long balance) {
            this.balance = balance;
        }
    }

    public static class PaymentRequest {
        // username больше не нужен - извлекается из JWT токена
        private Long amount;
        private Long orderId;
        private String description;

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class PaymentResponse {
        private Boolean success;
        private String transactionId;
        private Long remainingBalance;
        private String message;

        public Boolean getSuccess() {
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

