package ru.practicum.shareit.exception.model;

public class NotUniqueEmailException extends RuntimeException {
    public NotUniqueEmailException(String message) {
        super(message);
    }
}
