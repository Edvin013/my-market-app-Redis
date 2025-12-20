package ru.mirakyan.mymarket.payment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import ru.mirakyan.mymarket.payment.service.PaymentServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentServiceTest {

    private PaymentServiceImpl paymentService;
    private static final Long INITIAL_BALANCE = 100000L;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(INITIAL_BALANCE);
    }

    @Test
    void shouldReturnInitialBalance() {
        StepVerifier.create(paymentService.getBalance())
                .assertNext(balance -> assertThat(balance).isEqualTo(INITIAL_BALANCE))
                .verifyComplete();
    }

    @Test
    void shouldProcessPaymentSuccessfully() {
        Long amount = 5000L;
        Long orderId = 123L;
        String description = "Test payment";

        StepVerifier.create(paymentService.processPayment(amount, orderId, description))
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isTrue();
                    assertThat(result.getTransactionId()).isNotNull();
                    assertThat(result.getRemainingBalance()).isEqualTo(INITIAL_BALANCE - amount);
                    assertThat(result.getMessage()).contains("успешно");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailPaymentWhenInsufficientFunds() {
        Long amount = INITIAL_BALANCE + 1000L;
        Long orderId = 456L;
        String description = "Test payment";

        StepVerifier.create(paymentService.processPayment(amount, orderId, description))
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isFalse();
                    assertThat(result.getTransactionId()).isNull();
                    assertThat(result.getRemainingBalance()).isEqualTo(INITIAL_BALANCE);
                    assertThat(result.getMessage()).contains("Недостаточно");
                })
                .verifyComplete();
    }

    @Test
    void shouldFailPaymentWhenInvalidAmount() {
        Long amount = 0L;
        Long orderId = 789L;
        String description = "Test payment";

        StepVerifier.create(paymentService.processPayment(amount, orderId, description))
                .assertNext(result -> {
                    assertThat(result.isSuccess()).isFalse();
                    assertThat(result.getMessage()).contains("Неверная");
                })
                .verifyComplete();
    }

    @Test
    void shouldDeductBalanceAfterSuccessfulPayment() {
        Long amount = 3000L;
        Long orderId = 111L;

        paymentService.processPayment(amount, orderId, "Payment 1").block();

        StepVerifier.create(paymentService.getBalance())
                .assertNext(balance -> assertThat(balance).isEqualTo(INITIAL_BALANCE - amount))
                .verifyComplete();
    }

    @Test
    void shouldHandleMultiplePayments() {
        Long amount1 = 1000L;
        Long amount2 = 2000L;

        paymentService.processPayment(amount1, 1L, "Payment 1").block();
        paymentService.processPayment(amount2, 2L, "Payment 2").block();

        StepVerifier.create(paymentService.getBalance())
                .assertNext(balance -> assertThat(balance).isEqualTo(INITIAL_BALANCE - amount1 - amount2))
                .verifyComplete();
    }
}

