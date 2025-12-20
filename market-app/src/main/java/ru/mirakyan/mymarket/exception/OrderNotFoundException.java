package ru.mirakyan.mymarket.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Заказ не найден с id: " + id);
    }
}

