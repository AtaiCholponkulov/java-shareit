package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    ItemService itemService;
    @Autowired
    private MockMvc mvc;

    @Test
    void addItem() throws Exception {
        Item item = new Item(0,
                "item",
                "descr",
                true,
                new User(0, "user", "user@mail.com"),
                null);
        when(itemService.add(any(), anyInt()))
                .thenReturn(item);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(item))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(item.getId()), Integer.class),
                        jsonPath("$.name", is(item.getName())),
                        jsonPath("$.description", is(item.getDescription())),
                        jsonPath("$.available", is(item.getAvailable())),
                        jsonPath("$.requestId", nullValue()));
    }

    @Test
    void get() throws Exception {
        ItemDtoWithBookingsAndComments item = ItemDtoWithBookingsAndComments.builder()
                .id(0)
                .name("item")
                .description("descr")
                .available(false)
                .build();
        when(itemService.get(anyInt(), anyInt()))
                .thenReturn(item);

        mvc.perform(MockMvcRequestBuilders.get("/items/0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(item.getId()), Integer.class),
                        jsonPath("$.name", is(item.getName())),
                        jsonPath("$.description", is(item.getDescription())),
                        jsonPath("$.available", is(item.getAvailable())),
                        jsonPath("$.requestId", nullValue()));
    }

    @Test
    void getViewerItems() throws Exception {
        ItemDtoWithBookingsAndComments item = ItemDtoWithBookingsAndComments.builder()
                .id(0)
                .name("item")
                .description("descr")
                .available(false)
                .build();
        when(itemService.getViewerItems(anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(item));

        mvc.perform(MockMvcRequestBuilders.get("/items?from=0&size=3")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(item.getId()), Integer.class),
                        jsonPath("$[0].name", is(item.getName())),
                        jsonPath("$[0].description", is(item.getDescription())),
                        jsonPath("$[0].available", is(item.getAvailable())),
                        jsonPath("$[0].requestId", nullValue()));
    }

    @Test
    void search() throws Exception {
        Item item = new Item(0,
                "item",
                "descr",
                true,
                new User(0, "user", "user@mail.com"),
                null);
        when(itemService.search(anyString(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of(item));

        mvc.perform(MockMvcRequestBuilders.get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(item.getId()), Integer.class),
                        jsonPath("$[0].name", is(item.getName())),
                        jsonPath("$[0].description", is(item.getDescription())),
                        jsonPath("$[0].available", is(item.getAvailable())),
                        jsonPath("$[0].requestId", nullValue()));
    }

    @Test
    void update() throws Exception {
        Item item = new Item(0,
                "item",
                "descr",
                true,
                new User(0, "user", "user@mail.com"),
                null);
        ItemDto itemDto = ItemDto.builder().build();
        when(itemService.update(anyInt(), anyInt(), ArgumentMatchers.any(ItemDto.class)))
                .thenReturn(item);

        mvc.perform(patch("/items/0")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(item.getId()), Integer.class),
                        jsonPath("$[0].name", is(item.getName())),
                        jsonPath("$[0].description", is(item.getDescription())),
                        jsonPath("$[0].available", is(item.getAvailable())),
                        jsonPath("$[0].requestId", nullValue()));
    }

    @Test
    void addComment() throws Exception {
        Comment comment = new Comment(0, "text", null, new User(null, "author", null), null);
        CommentDto commentDto = CommentDto.builder().build();
        when(itemService.add(ArgumentMatchers.any(CommentDto.class), anyInt(), anyInt()))
                .thenReturn(comment);

        mvc.perform(post("/items/0/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(comment.getId()), Integer.class),
                        jsonPath("$[0].text", is(comment.getText())),
                        jsonPath("$[0].authorName", is(comment.getAuthor().getName())),
                        jsonPath("$[0].created", nullValue()));
    }
}