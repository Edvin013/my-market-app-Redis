package ru.mirakyan.mymarket.exception;

public class EmptyCartException extends RuntimeException {
    public EmptyCartException() {
        super("Корзина пуста");
    }
}

