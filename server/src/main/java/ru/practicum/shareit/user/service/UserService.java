package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
public interface UserService {
    @Transactional
    User add(User user);

    User get(int userId);

    @Transactional
    User update(User changedUser);

    @Transactional
    void delete(int userId);

    List<User> getAll();
}
