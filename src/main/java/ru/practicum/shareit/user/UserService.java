package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("InMemoryUserStorage")
    private final UserStorage userStorage;

    public User add(User user) {
        return userStorage.add(user);
    }

    public User get(int userId) {
        return userStorage.get(userId);
    }

    public User update(int userId, User user) {
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
