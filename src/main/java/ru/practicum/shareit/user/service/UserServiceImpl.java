package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public User add(User user) {
        return userRepository.save(user);
    }

    @Override
    public User get(int userId) {
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Такого пользователя нет в базе id=" + userId));
    }

    @Transactional
    @Override
    public User update(User changedUser) {
        User dbUser = this.get(changedUser.getId());
        dbUser.update(changedUser);
        return userRepository.save(dbUser);
    }

    @Transactional
    @Override
    public void delete(int userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
