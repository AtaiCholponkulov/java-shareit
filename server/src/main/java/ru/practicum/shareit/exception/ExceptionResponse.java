package ru.practicum.shareit.exception;

import lombok.Getter;

@Getter
public class ExceptionResponse {
    private final String error;

    public ExceptionResponse(String error) {
        this.error = error;
    }
}
