package ru.practicum.shareit.validator;

import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class Validator {

    public static void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank())
            throw new ValidationException("Имя пользователя не может быть пустым.");
        if (user.getEmail() == null)
            throw new ValidationException("Электронная почта пользователя не может быть пустой.");
        if (!user.getEmail().matches("^\\w+(?:\\.\\w+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"))
            throw new ValidationException("Введеный email не соответствует формату.");
    }

    public static void validateUpdateUser(User user) {
        String name = user.getName();
        String email = user.getEmail();
        if ((name == null || name.isBlank()) && email == null)
            throw new ValidationException("Пустое обновление не допустимо.");
        if (email != null && !user.getEmail().matches("^\\w+(?:\\.\\w+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$"))
            throw new ValidationException("Введеный email не соответствует формату.");
    }

    public static void validateItem(Item item) {
        if (item.getName() == null || item.getName().isBlank())
            throw new ValidationException("Название предмета не может быть пустым.");
        if (item.getDescription() == null)
            throw new ValidationException("Описание предмета не может быть пустым.");
        if (item.getAvailable() == null)
            throw new ValidationException("Статус предмета не может быть пустым.");
    }

    public static void validateUpdateItem(Item item) {
        String name = item.getName();
        String description = item.getDescription();
        Boolean available = item.getAvailable();
        if ((name == null || name.isBlank())
                && (description == null || description.isBlank())
                && available == null)
            throw new ValidationException("Пустое обновление не допустимо.");
    }
}
