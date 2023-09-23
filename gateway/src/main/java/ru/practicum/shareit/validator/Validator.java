package ru.practicum.shareit.validator;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.filter.BookingFilter;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

public class Validator {

    public static void validateUser(UserDto user) {
        if (user.getName() == null || user.getName().isBlank()) {
            throw new ValidationException("Имя пользователя не может быть пустым.");
        }
        if (user.getEmail() == null) {
            throw new ValidationException("Электронная почта пользователя не может быть пустой.");
        }
        if (!user.getEmail().matches("^\\w+(?:\\.\\w+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            throw new ValidationException("Введеный email не соответствует формату.");
        }
    }

    public static void validateUpdateUser(UserDto user) {
        String name = user.getName();
        String email = user.getEmail();
        if ((name == null || name.isBlank()) && email == null) {
            throw new ValidationException("Пустое обновление не допустимо.");
        }
        if (email != null && !user.getEmail().matches("^\\w+(?:\\.\\w+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            throw new ValidationException("Введеный email не соответствует формату.");
        }
    }

    public static void validateItem(ItemDto item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Название вещи не может быть пустым.");
        }
        if (item.getDescription() == null) {
            throw new ValidationException("Описание вещи не может быть пустым.");
        }
        if (item.getAvailable() == null) {
            throw new ValidationException("Статус вещи не может быть пустым.");
        }
    }

    public static void validateUpdateItem(ItemDto item) {
        String name = item.getName();
        String description = item.getDescription();
        Boolean available = item.getAvailable();
        if ((name == null || name.isBlank())
                && (description == null || description.isBlank())
                && available == null) {
            throw new ValidationException("Пустое обновление не допустимо.");
        }
    }

    public static void validate(BookingDtoIn bookingDtoIn) {
        LocalDateTime start = bookingDtoIn.getStart();
        LocalDateTime end = bookingDtoIn.getEnd();
        LocalDateTime now = LocalDateTime.now();
        if (bookingDtoIn.getItemId() == null
                || start == null
                || end == null
                || !now.isBefore(start)
                || !start.isBefore(end)) {
            throw new ValidationException("Ошибка в теле запроса");
        }
    }

    public static void validate(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Пустой комментарий");
        }
    }

    public static void validate(ItemRequestDto itemRequest) {
        if (itemRequest.getDescription() == null || itemRequest.getDescription().isBlank()) {
            throw new ValidationException("Пустой запрос");
        }
    }

    public static boolean validatePaginationParams(Integer from, Integer size) {
        if (from != null && from >= 0 && size != null && size > 0) {
            return true;//pagination
        }
        if (from == null && size == null) {
            return false;//no pagination
        }
        throw new ValidationException("Ошибочные параметры запроса");
    }

    public static BookingFilter validateState(String state) {
        try {
            return BookingFilter.valueOf(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
