package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    UserService userService;
    @Autowired
    private MockMvc mvc;

    @Test
    void add() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        when(userService.add(any(User.class)))
                .thenReturn(user);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(user.getId()), Integer.class),
                        jsonPath("$.name", is(user.getName())),
                        jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void addUserNoNameThrowsException() throws Exception {
        User user = new User(0, null, "user@mail.com");
        when(userService.add(any(User.class)))
                .thenReturn(user);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUserNoEmailThrowsException() throws Exception {
        User user = new User(0, "name", null);
        when(userService.add(any(User.class)))
                .thenReturn(user);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUserWrongEmailThrowsException() throws Exception {
        User user = new User(0, "name", "name@mail");
        when(userService.add(any(User.class)))
                .thenReturn(user);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        when(userService.get(0))
                .thenReturn(user);

        mvc.perform(MockMvcRequestBuilders.get("/users/0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(user.getId()), Integer.class),
                        jsonPath("$.name", is(user.getName())),
                        jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void update() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        when(userService.update(any(User.class)))
                .thenReturn(user);

        mvc.perform(MockMvcRequestBuilders.patch("/users/0")
                        .content(mapper.writeValueAsString(user))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(user.getId()), Integer.class),
                        jsonPath("$.name", is(user.getName())),
                        jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void updateUserNoUpdateThrowsException() throws Exception {
        User user = new User(0, null, "user@mail.com");
        when(userService.update(any(User.class)))
                .thenReturn(user);
        UserDto userDto = UserDto.builder().name(null).email(null).build();

        mvc.perform(MockMvcRequestBuilders.patch("/users/0")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUserWrongEmailThrowsException() throws Exception {
        User user = new User(0, "name", null);
        when(userService.update(any(User.class)))
                .thenReturn(user);
        UserDto userDto = UserDto.builder().name(null).email("email@com").build();

        mvc.perform(MockMvcRequestBuilders.patch("/users/0")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete() throws Exception {
        doNothing().when(userService).delete(0);

        mvc.perform(MockMvcRequestBuilders.delete("/users/0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getAll() throws Exception {
        User user1 = new User(0, "user1", "user1@mail.com");
        User user2 = new User(1, "user2", "user2@mail.com");
        when(userService.getAll())
                .thenReturn(List.of(user1, user2));

        mvc.perform(MockMvcRequestBuilders.get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(2)),
                        jsonPath("$[0].id", is(user1.getId()), Integer.class),
                        jsonPath("$[0].name", is(user1.getName())),
                        jsonPath("$[0].email", is(user1.getEmail())),
                        jsonPath("$[1].id", is(user2.getId()), Integer.class),
                        jsonPath("$[1].name", is(user2.getName())),
                        jsonPath("$[1].email", is(user2.getEmail())));
    }
}