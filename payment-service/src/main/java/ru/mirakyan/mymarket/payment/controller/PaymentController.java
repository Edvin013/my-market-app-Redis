package ru.mirakyan.mymarket.payment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.mirakyan.mymarket.payment.api.PaymentApi;
import ru.mirakyan.mymarket.payment.model.BalanceResponse;
import ru.mirakyan.mymarket.payment.model.PaymentRequest;
import ru.mirakyan.mymarket.payment.model.PaymentResponse;
import ru.mirakyan.mymarket.payment.service.PaymentService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {

    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        log.debug("REST: Получение баланса");
        
        String username = exchange.getRequest().getQueryParams().getFirst("username");
        if (username == null || username.isEmpty()) {
            log.warn("REST: Не указан username");
            return Mono.just(ResponseEntity.badRequest().build());
        }
        
        return paymentService.getBalance(username)
                .map(balance -> {
                    BalanceResponse response = new BalanceResponse();
                    response.setBalance(balance);
                    return ResponseEntity.ok(response);
                })
                .doOnSuccess(resp -> log.debug("REST: Баланс получен для {}: {}", username, resp.getBody()));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(
            Mono<PaymentRequest> paymentRequest,
            ServerWebExchange exchange) {

        log.debug("REST: Обработка платежа");
        return paymentRequest
                .flatMap(request -> {
                    String username = request.getUsername();
                    if (username == null || username.isEmpty()) {
                        log.warn("REST: Не указан username");
                        PaymentResponse errorResponse = new PaymentResponse();
                        errorResponse.setSuccess(false);
                        errorResponse.setMessage("Username обязателен");
                        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                    }
                    
                    log.debug("REST: Запрос на платеж для {}: сумма={}, orderId={}",
                             username, request.getAmount(), request.getOrderId());
                    return paymentService.processPayment(
                            username,
                            request.getAmount(),
                            request.getOrderId(),
                            request.getDescription()
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
                });
    }
}

