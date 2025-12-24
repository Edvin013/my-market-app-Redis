package ru.mirakyan.mymarket.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final ConcurrentHashMap<String, AtomicLong> userBalances = new ConcurrentHashMap<>();
    private final Long initialBalance;

    public PaymentServiceImpl(@Value("${payment.initial-balance:1000000}") Long initialBalance) {
        this.initialBalance = initialBalance;
        log.info("Сервис платежей инициализирован с начальным балансом: {} копеек", initialBalance);
    }

    @Override
    public Mono<Long> getBalance(String username) {
        AtomicLong balance = userBalances.computeIfAbsent(username, k -> new AtomicLong(initialBalance));
        long currentBalance = balance.get();
        log.debug("Получение баланса для {}: {}", username, currentBalance);
        return Mono.just(currentBalance);
    }

    @Override
    public Mono<PaymentResult> processPayment(String username, Long amount, Long orderId, String description) {
        return Mono.fromCallable(() -> {
            log.info("Обработка платежа для {}: сумма={}, orderId={}, описание={}",
                     username, amount, orderId, description);

            if (amount == null || amount <= 0) {
                log.warn("Неверная сумма платежа: {}", amount);
                AtomicLong balance = userBalances.computeIfAbsent(username, k -> new AtomicLong(initialBalance));
                return new PaymentResult(false, null, balance.get(), "Неверная сумма платежа");
            }

            AtomicLong balance = userBalances.computeIfAbsent(username, k -> new AtomicLong(initialBalance));
            long currentBalance = balance.get();

            if (currentBalance < amount) {
                log.warn("Недостаточно средств для {}: баланс={}, сумма={}", username, currentBalance, amount);
                return new PaymentResult(false, null, currentBalance, "Недостаточно средств на балансе");
            }

            long newBalance = balance.addAndGet(-amount);
            String transactionId = "txn_" + UUID.randomUUID();

            log.info("Платеж успешно обработан для {}: transactionId={}, новыйБаланс={}",
                     username, transactionId, newBalance);

            return new PaymentResult(true, transactionId, newBalance, "Платеж успешно обработан");
        });
    }
}

