package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    ObjectMapper mapper;
    @MockBean
    BookingService bookingService;
    @Autowired
    private MockMvc mvc;

    @Test
    void add() throws Exception {
        BookingDtoIn bookingDtoIn = new BookingDtoIn(0, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        User user = new User(0, "user", "user@mail.com");
        Item item = new Item(0, "item", "descr", true, user, null);
        Booking booking = new Booking(0, null, null, item, user, BookingStatus.APPROVED);
        when(bookingService.add(anyInt(), any(BookingDtoIn.class)))
                .thenReturn(booking);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(booking.getId()), Integer.class),
                        jsonPath("$.start", nullValue()),
                        jsonPath("$.end", nullValue()),
                        jsonPath("$.item.name", is(item.getName())),
                        jsonPath("$.booker.name", is(user.getName())),
                        jsonPath("$.status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void get() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        Item item = new Item(0, "item", "descr", true, user, null);
        Booking booking = new Booking(0, null, null, item, user, BookingStatus.APPROVED);
        when(bookingService.get(anyInt(), anyInt()))
                .thenReturn(booking);

        mvc.perform(MockMvcRequestBuilders.get("/bookings/0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("X-Sharer-User-Id", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(booking.getId()), Integer.class),
                        jsonPath("$.start", nullValue()),
                        jsonPath("$.end", nullValue()),
                        jsonPath("$.item.name", is(item.getName())),
                        jsonPath("$.booker.name", is(user.getName())),
                        jsonPath("$.status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void update() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        Item item = new Item(0, "item", "descr", true, user, null);
        Booking booking = new Booking(0, null, null, item, user, BookingStatus.APPROVED);
        when(bookingService.update(0, true, 0))
                .thenReturn(booking);

        mvc.perform(patch("/bookings/0?approved=true")
                        .header("X-Sharer-User-Id", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id", is(booking.getId()), Integer.class),
                        jsonPath("$.start", nullValue()),
                        jsonPath("$.end", nullValue()),
                        jsonPath("$.item.name", is(item.getName())),
                        jsonPath("$.booker.name", is(user.getName())),
                        jsonPath("$.status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void getUserBookings() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        Item item = new Item(0, "item", "descr", true, user, null);
        Booking booking = new Booking(0, null, null, item, user, BookingStatus.APPROVED);
        when(bookingService.getUserBookings(0, null, null, "ALL"))
                .thenReturn(List.of(booking));

        mvc.perform(MockMvcRequestBuilders.get("/bookings")
                        .header("X-Sharer-User-Id", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(booking.getId()), Integer.class),
                        jsonPath("$[0].start", nullValue()),
                        jsonPath("$[0].end", nullValue()),
                        jsonPath("$[0].item.name", is(item.getName())),
                        jsonPath("$[0].booker.name", is(user.getName())),
                        jsonPath("$[0].status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void getBookingsOfUserItems() throws Exception {
        User user = new User(0, "user", "user@mail.com");
        Item item = new Item(0, "item", "descr", true, user, null);
        Booking booking = new Booking(0, null, null, item, user, BookingStatus.APPROVED);
        when(bookingService.getBookingsOfUserItems(0, null, null, "ALL"))
                .thenReturn(List.of(booking));

        mvc.perform(MockMvcRequestBuilders.get("/bookings/owner")
                        .header("X-Sharer-User-Id", 0)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$", hasSize(1)),
                        jsonPath("$[0].id", is(booking.getId()), Integer.class),
                        jsonPath("$[0].start", nullValue()),
                        jsonPath("$[0].end", nullValue()),
                        jsonPath("$[0].item.name", is(item.getName())),
                        jsonPath("$[0].booker.name", is(user.getName())),
                        jsonPath("$[0].status", is(BookingStatus.APPROVED.toString())));
    }
}