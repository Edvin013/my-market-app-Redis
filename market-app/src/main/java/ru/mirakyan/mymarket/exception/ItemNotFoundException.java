package ru.mirakyan.mymarket.exception;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(Long id) {
        super("Товар не найден с id: " + id);
    }
}

