package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceImplTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final UserService userService = new UserServiceImpl(userRepository);

    @Test
    void get() {
        User user = new User(0, "name", "email@com");
        Mockito.when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        User answer = userService.get(user.getId());

        assertEquals(user.getId(), answer.getId());
        assertEquals(user.getName(), answer.getName());
        assertEquals(user.getEmail(), answer.getEmail());
    }

    @Test
    void getThrowsException() {
        Mockito.when(userRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.get(1));
    }

    @Test
    void update() {
        User dbUser = new User(0, "name", "email@com");
        User changedUser = new User(0, "newName", "newemail@com");
        Mockito.when(userRepository.findById(changedUser.getId()))
                .thenReturn(Optional.of(dbUser));
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        User answer = userService.update(changedUser);
        assertEquals(changedUser.getId(), answer.getId());
        assertEquals(changedUser.getName(), answer.getName());
        assertEquals(changedUser.getEmail(), answer.getEmail());
    }

    @Test
    void updateThrowsException() {
        User noSuchUser = new User(1, "noname", "noemail@com");
        Mockito.when(userRepository.findById(noSuchUser.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(noSuchUser));
    }
}