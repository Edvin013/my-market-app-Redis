package ru.mirakyan.mymarket.payment.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private Long remainingBalance;
    private String message;

}

