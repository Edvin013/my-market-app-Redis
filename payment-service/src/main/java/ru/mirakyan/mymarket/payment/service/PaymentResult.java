package ru.mirakyan.mymarket.payment.service;

public class PaymentResult {
    private boolean success;
    private String transactionId;
    private Long remainingBalance;
    private String message;

    public PaymentResult() {
    }

    public PaymentResult(boolean success, String transactionId, Long remainingBalance, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.remainingBalance = remainingBalance;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
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

