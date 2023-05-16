package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User add(@RequestBody User user) {
        return userService.add(user);
    }

    @GetMapping("/{userId}")
    public User get(@PathVariable int userId) {
        return userService.get(userId);
    }

    @PatchMapping("/{userId}")
    public User update(@PathVariable int userId, @RequestBody User user) {
        return userService.update(userId, user);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable int userId) {
        userService.delete(userId);
    }

    @GetMapping
    public List<User> getAll() {
        return userService.getAll();
    }
}
