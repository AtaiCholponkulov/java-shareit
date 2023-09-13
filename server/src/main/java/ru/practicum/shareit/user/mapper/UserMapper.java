package ru.practicum.shareit.user.mapper;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static User map(UserDto userDto, Integer userId) {
        return new User(userId, userDto.getName(), userDto.getEmail());
    }

    public static User map(UserDto userDto) {
        return map(userDto, null);
    }

    public static UserDto map(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public static List<UserDto> map(List<User> users) {
        return users.stream().map(UserMapper::map).collect(Collectors.toList());
    }
}
