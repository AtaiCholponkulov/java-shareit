package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.NotUniqueEmailException;
import ru.practicum.shareit.exception.model.BadRequestException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleInvalidRequestBodyException(final BadRequestException e) {
        log.warn(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ExceptionResponse handleNotFoundException(final NotFoundException e) {
        log.warn(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ExceptionResponse handleNotUniqueEmailException(final NotUniqueEmailException e) {
        log.warn(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionResponse handleThrowable(final Throwable e) {
        log.warn(e.getMessage(), e);
        return new ExceptionResponse(e.getMessage());
    }
}
