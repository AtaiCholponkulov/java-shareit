package ru.practicum.shareit.user;

import java.util.List;

public interface UserStorage {

    User add(User user);

    User get(int userId);

    User update(User user);

    void delete(int userId);

    List<User> getAll();
}
