package ru.mirakyan.mymarket.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final AtomicLong balance;

    public PaymentServiceImpl(@Value("${payment.initial-balance:1000000}") Long initialBalance) {
        this.balance = new AtomicLong(initialBalance);
        log.info("Сервис платежей инициализирован с балансом: {} копеек", initialBalance);
    }

    @Override
    public Mono<Long> getBalance() {
        long currentBalance = balance.get();
        log.debug("Получение баланса: {}", currentBalance);
        return Mono.just(currentBalance);
    }

    @Override
    public Mono<PaymentResult> processPayment(Long amount, Long orderId, String description) {
        return Mono.fromCallable(() -> {
            log.info("Обработка платежа: сумма={}, orderId={}, описание={}",
                     amount, orderId, description);

            if (amount == null || amount <= 0) {
                log.warn("Неверная сумма платежа: {}", amount);
                return new PaymentResult(false, null, balance.get(), "Неверная сумма платежа");
            }

            long currentBalance = balance.get();

            if (currentBalance < amount) {
                log.warn("Недостаточно средств: баланс={}, сумма={}", currentBalance, amount);
                return new PaymentResult(false, null, currentBalance, "Недостаточно средств на балансе");
            }

            long newBalance = balance.addAndGet(-amount);
            String transactionId = "txn_" + UUID.randomUUID();

            log.info("Платеж успешно обработан: transactionId={}, новыйБаланс={}",
                     transactionId, newBalance);

            return new PaymentResult(true, transactionId, newBalance, "Платеж успешно обработан");
        });
    }
}

