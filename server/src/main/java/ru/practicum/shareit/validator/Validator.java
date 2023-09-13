package ru.practicum.shareit.validator;

public class Validator {

    public static boolean isForPagination(Integer from, Integer size) {
        return from != null || size != null;
    }
}
