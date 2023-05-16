package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {

    User add(User user);

    User get(int userId);

    User update(User user);

    void delete(int userId);

    List<User> getAll();
}
