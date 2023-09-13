package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static ru.practicum.shareit.user.mapper.UserMapper.map;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto add(@RequestBody UserDto userDto) {
        User user = map(userDto);
        return map(userService.add(user));
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable int userId) {
        return map(userService.get(userId));
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable int userId,
                          @RequestBody UserDto userDto) {
        User user = map(userDto, userId);
        return map(userService.update(user));
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable int userId) {
        userService.delete(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return map(userService.getAll());
    }
}
