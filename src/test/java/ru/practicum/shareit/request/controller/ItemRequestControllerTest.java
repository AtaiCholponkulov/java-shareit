package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;

    @Test
    void add() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("descr").build();
        ItemRequest itemRequest = new ItemRequest(0, itemRequestDto.getDescription(), user, null);
        when(itemRequestService.add(0, itemRequestDto))
                .thenReturn(itemRequest);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(itemRequest.getId()), Integer.class),
                        jsonPath("$.description", is(itemRequest.getDescription())),
                        jsonPath("$.created", nullValue()));
    }

    @Test
    void addThrowsException() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description(null).build();
        ItemRequest itemRequest = new ItemRequest(0, itemRequestDto.getDescription(), user, null);
        when(itemRequestService.add(0, itemRequestDto))
                .thenReturn(itemRequest);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRequests() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(0)
                .description("descr")
                .created(LocalDateTime.now())
                .build();
        ItemRequest itemRequest = new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                user,
                itemRequestDto.getCreated());
        when(itemRequestService.getUserRequests(0))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(MockMvcRequestBuilders.get("/requests")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(itemRequest.getId()), Integer.class),
                        jsonPath("$[0].description", is(itemRequest.getDescription())),
                        jsonPath("$[0].created", is(itemRequest.getCreated().toString())));
    }

    @Test
    void get() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(0)
                .description("descr")
                .created(LocalDateTime.now())
                .build();
        ItemRequest itemRequest = new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                user,
                itemRequestDto.getCreated());
        when(itemRequestService.get(0, 0))
                .thenReturn(itemRequestDto);

        mvc.perform(MockMvcRequestBuilders.get("/requests/0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(itemRequest.getId()), Integer.class),
                        jsonPath("$.description", is(itemRequest.getDescription())));
    }

    @Test
    void getAll() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .id(0)
                .description("descr")
                .created(LocalDateTime.now())
                .build();
        ItemRequest itemRequest = new ItemRequest(
                itemRequestDto.getId(),
                itemRequestDto.getDescription(),
                user,
                itemRequestDto.getCreated());
        when(itemRequestService.get(null, null, 0))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(MockMvcRequestBuilders.get("/requests/all")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(itemRequest.getId()), Integer.class),
                        jsonPath("$[0].description", is(itemRequest.getDescription())));
    }
}
