package ru.practicum.shareit.user.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.NotUniqueEmailException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

import static ru.practicum.shareit.validator.Validator.validateUpdateUser;
import static ru.practicum.shareit.validator.Validator.validateUser;

@Component("InMemoryUserStorage")
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> db = new HashMap<>();
    private int id = 1;

    private int getId() {
        return id++;
    }

    @Override
    public User add(User user) {
        validateUser(user);
        if (db.values().stream().map(User::getEmail).anyMatch(email -> email.equals(user.getEmail())))
            throw new NotUniqueEmailException("Данный почтовый адресс уже занят.");
        user.setId(getId());
        db.put(user.getId(), user);
        log.info("Добавлен новый пользователь id={}", user.getId());
        return user;
    }

    @Override
    public User get(int userId) {
        return Optional.ofNullable(db.get(userId))
                .orElseThrow(() -> new NotFoundException("Такого пользователя нет в базе id=" + userId));
    }

    @Override
    public User update(User user) {
        validateUpdateUser(user);
        User dbUser = this.get(user.getId());
        if (db.values()
                .stream()
                .anyMatch(curUser -> curUser.getEmail().equals(user.getEmail())
                        && !Objects.equals(curUser.getId(), user.getId())))
            throw new NotUniqueEmailException("Данный почтовый адресс уже занят.");
        dbUser.update(user);
        log.info("Обновлен пользователь id={}", user.getId());
        return dbUser;
    }

    @Override
    public void delete(int userId) {
        this.get(userId);
        db.remove(userId);
        log.info("Удален пользователь id={}", userId);
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(db.values());
    }
}
