package ru.mirakyan.mymarket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import ru.mirakyan.mymarket.service.impl.PaymentClientImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentClientIntegrationTest {

    private MockWebServer mockWebServer;
    private PaymentClient paymentClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        paymentClient = new PaymentClientImpl(webClient);
        objectMapper = new ObjectMapper();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldGetBalanceSuccessfully() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("balance", 100000L);

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(response)));

        StepVerifier.create(paymentClient.getBalance())
                .assertNext(balance -> assertThat(balance).isEqualTo(100000L))
                .verifyComplete();
    }

    @Test
    void shouldReturnZeroBalanceWhenServiceUnavailable() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        StepVerifier.create(paymentClient.getBalance())
                .assertNext(balance -> assertThat(balance).isEqualTo(0L))
                .verifyComplete();
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("transactionId", "txn_123");
        response.put("remainingBalance", 95000L);
        response.put("message", "Платеж успешно обработан");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(response)));

        StepVerifier.create(paymentClient.processPayment(5000L, 123L, "Test payment"))
                .assertNext(success -> assertThat(success).isTrue())
                .verifyComplete();
    }

    @Test
    void shouldFailPaymentWhenInsufficientFunds() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("remainingBalance", 100000L);
        response.put("message", "Недостаточно средств");

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(objectMapper.writeValueAsString(response)));

        StepVerifier.create(paymentClient.processPayment(150000L, 456L, "Test payment"))
                .assertNext(success -> assertThat(success).isFalse())
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenPaymentServiceUnavailable() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        StepVerifier.create(paymentClient.processPayment(5000L, 789L, "Test payment"))
                .assertNext(success -> assertThat(success).isFalse())
                .verifyComplete();
    }

    @Test
    void shouldCheckServiceAvailability() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200));

        StepVerifier.create(paymentClient.isServiceAvailable())
                .assertNext(available -> assertThat(available).isTrue())
                .verifyComplete();
    }

    @Test
    void shouldReturnFalseWhenServiceNotAvailable() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        StepVerifier.create(paymentClient.isServiceAvailable())
                .assertNext(available -> assertThat(available).isFalse())
                .verifyComplete();
    }
}

