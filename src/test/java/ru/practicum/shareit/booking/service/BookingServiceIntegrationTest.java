package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceIntegrationTest {

    private final EntityManager em;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Test
    void add() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = assertDoesNotThrow(() -> bookingService.add(user2.getId(), bookingData));
        em.detach(addedBooking);
        Booking dbBooking = em
                .createQuery("select b from Booking b where b.id = :id", Booking.class)
                .setParameter("id", addedBooking.getId())
                .getSingleResult();
        assertThat(dbBooking.getStatus(), is(addedBooking.getStatus()));
        assertThat(dbBooking.getItem().getName(), is(addedBooking.getItem().getName()));
        assertThat(dbBooking.getBooker().getName(), is(addedBooking.getBooker().getName()));
    }

    @Test
    void addItemNotAvailable() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(false)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        assertThrows(ValidationException.class, () -> bookingService.add(user2.getId(), bookingData));
    }

    @Test
    void addThrowsNotFoundException() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        assertThrows(NotFoundException.class, () -> bookingService.add(user1.getId(), bookingData));
    }

    @Test
    void getViewerIsNotBookerOrItemOwner() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking dbBooking = bookingService.add(user2.getId(), bookingData);
        assertThrows(NotFoundException.class, () -> bookingService.get(10, dbBooking.getId()));
    }

    @Test
    void getViewerIsBookerOrItemOwner() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        Booking dbBooking = assertDoesNotThrow(() -> bookingService.get(user2.getId(), addedBooking.getId()));
        assertThat(dbBooking.getStatus(), is(addedBooking.getStatus()));
        assertThat(dbBooking.getItem().getName(), is(addedBooking.getItem().getName()));
        assertThat(dbBooking.getBooker().getName(), is(addedBooking.getBooker().getName()));
    }

    @Test
    void get() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        Booking dbBooking = bookingService.get(addedBooking.getId());
        assertThat(dbBooking.getStatus(), is(addedBooking.getStatus()));
        assertThat(dbBooking.getItem().getName(), is(addedBooking.getItem().getName()));
        assertThat(dbBooking.getBooker().getName(), is(addedBooking.getBooker().getName()));
    }

    @Test
    void getThrowsNotFoundException() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        assertThrows(NotFoundException.class, () -> bookingService.get(10));
    }

    @Test
    void updateThrowsNotFoundException() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        assertThrows(NotFoundException.class, () -> bookingService.update(user2.getId(), true, addedBooking.getId()));
    }

    @Test
    void updateThrowsValidationException() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        assertDoesNotThrow(() -> bookingService.update(user1.getId(), true, addedBooking.getId()));
        assertThrows(ValidationException.class, () -> bookingService.update(user1.getId(), true, addedBooking.getId()));
    }

    @Test
    void update() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        Booking dbBooking = assertDoesNotThrow(() -> bookingService.update(user1.getId(), true, addedBooking.getId()));
        assertThat(dbBooking.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    void getUserBookingsNoPagination() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        List<Booking> user2Bookings = bookingService.getUserBookings(user2.getId(), null, null, "ALL");
        assertThat(user2Bookings, hasSize(1));
        assertThat(user2Bookings.get(0).getStatus(), is(addedBooking.getStatus()));
        assertThat(user2Bookings.get(0).getItem().getName(), is(addedBooking.getItem().getName()));
        assertThat(user2Bookings.get(0).getBooker().getName(), is(addedBooking.getBooker().getName()));
    }

    @Test
    void getUserBookingsWithPagination() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        List<Booking> user2Bookings = bookingService.getUserBookings(user2.getId(), 0, 1, "ALL");
        assertThat(user2Bookings, hasSize(1));
        assertThat(user2Bookings.get(0).getStatus(), is(addedBooking.getStatus()));
        assertThat(user2Bookings.get(0).getItem().getName(), is(addedBooking.getItem().getName()));
        assertThat(user2Bookings.get(0).getBooker().getName(), is(addedBooking.getBooker().getName()));
    }

    @Test
    void getUserBookingsWrongPagination() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        assertThrows(ValidationException.class, () -> bookingService.getUserBookings(user2.getId(), null, 1, "ALL"));
    }

    @Test
    void getBookingsOfUserItemsWithPagination() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        List<Booking> user1ItemsBookings = bookingService.getBookingsOfUserItems(user1.getId(), 0, 1, "ALL");
        assertThat(user1ItemsBookings, hasSize(1));
        assertThat(user1ItemsBookings.get(0).getStatus(), is(addedBooking.getStatus()));
        assertThat(user1ItemsBookings.get(0).getItem().getName(), is(addedBooking.getItem().getName()));
        assertThat(user1ItemsBookings.get(0).getBooker().getName(), is(addedBooking.getBooker().getName()));
    }

    @Test
    void getBookingsOfUserItemsNoPagination() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        List<Booking> user1ItemsBookings = bookingService.getBookingsOfUserItems(user1.getId(), null, null, "ALL");
        assertThat(user1ItemsBookings, hasSize(1));
        assertThat(user1ItemsBookings.get(0).getStatus(), is(addedBooking.getStatus()));
        assertThat(user1ItemsBookings.get(0).getItem().getName(), is(addedBooking.getItem().getName()));
        assertThat(user1ItemsBookings.get(0).getBooker().getName(), is(addedBooking.getBooker().getName()));
    }

    @Test
    void getBookingsOfUserItemsWrongPagination() {
        User user1 = userService.add(new User(null, "owner", "owner@com"));
        User user2 = userService.add(new User(null, "booker", "booker@com"));
        Item user1Item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .description("descr")
                        .available(true)
                        .build(),
                user1.getId());
        BookingDtoIn bookingData = new BookingDtoIn(user1Item.getId(), NOW.minusDays(2), NOW.minusDays(1));

        Booking addedBooking = bookingService.add(user2.getId(), bookingData);
        em.detach(addedBooking);
        assertThrows(ValidationException.class, () -> bookingService.getBookingsOfUserItems(user1.getId(), null, 1, "ALL"));
    }
}