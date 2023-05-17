package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static ru.practicum.shareit.user.mapper.UserMapper.map;

@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("InMemoryUserStorage")
    private final UserStorage userStorage;

    public User add(UserDto userDto) {
        User user = map(userDto);
        return userStorage.add(user);
    }

    public User get(int userId) {
        return userStorage.get(userId);
    }

    public User update(int userId, UserDto userDto) {
        User user = map(userDto);
        user.setId(userId);
        return userStorage.update(user);
    }

    public void delete(int userId) {
        userStorage.delete(userId);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }
}
