package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;

import static ru.practicum.shareit.validator.Validator.validateUpdateUser;
import static ru.practicum.shareit.validator.Validator.validateUser;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody UserDto userDto) {
        validateUser(userDto);
        return userClient.addUser(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> get(@PathVariable int userId) {
        return userClient.getUser(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable int userId,
                                         @RequestBody UserDto userDto) {
        validateUpdateUser(userDto);
        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable int userId) {
        userClient.deleteUser(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return userClient.getAll();
    }
}
