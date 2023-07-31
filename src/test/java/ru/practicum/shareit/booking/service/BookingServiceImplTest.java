package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceImplTest {

    private final BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
    private final ItemService itemService = Mockito.mock(ItemServiceImpl.class);
    private final UserService userService = Mockito.mock(UserServiceImpl.class);
    private final BookingService bookingService = new BookingServiceImpl(bookingRepository, itemService, userService);
    private static User booker;
    private static User owner;
    private static Item item;
    private static BookingDtoIn bookingDtoIn;
    private static Booking booking;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void beforeEach() {
        booker = new User(0, "booker", "booker@com");
        owner = new User(1, "owner", "owner@com");
        item = new Item(0, "item", "description", true, owner, null);
        bookingDtoIn = new BookingDtoIn(item.getId(), NOW.minusDays(2), NOW.minusDays(1));
        booking = new Booking(0, NOW.minusDays(2), NOW.minusDays(1), item, booker, BookingStatus.WAITING);
    }

    @Test
    void add() {
        Mockito.when(userService.get(booker.getId()))
                .thenReturn(booker);
        Mockito.when(itemService.get(bookingDtoIn.getItemId()))
                .thenReturn(item);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking answer = assertDoesNotThrow(() -> bookingService.add(booker.getId(), bookingDtoIn));
        assertNull(answer.getId());
        assertEquals(BookingStatus.WAITING, answer.getStatus());
        assertEquals(bookingDtoIn.getStart(), answer.getStartDate());
        assertEquals(bookingDtoIn.getEnd(), answer.getEndDate());
        assertEquals(bookingDtoIn.getItemId(), answer.getItem().getId());
        assertEquals(booker.getId(), answer.getBooker().getId());
    }

    @Test
    void addThrowsValidationException() {
        item.setAvailable(false);

        Mockito.when(userService.get(booker.getId()))
                .thenReturn(booker);
        Mockito.when(itemService.get(bookingDtoIn.getItemId()))
                .thenReturn(item);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(ValidationException.class, () -> bookingService.add(booker.getId(), bookingDtoIn));
    }

    @Test
    void addThrowsNotFoundException() {
        Mockito.when(userService.get(booker.getId()))
                .thenReturn(booker);
        Mockito.when(itemService.get(bookingDtoIn.getItemId()))
                .thenReturn(item);
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(NotFoundException.class, () -> bookingService.add(owner.getId(), bookingDtoIn));
    }

    @Test
    void getViewerIsBooker() {
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        Booking answer = bookingService.get(booker.getId(), booking.getId());

        assertEquals(booking.getId(), answer.getId());
        assertEquals(booking.getStatus(), answer.getStatus());
        assertEquals(booking.getStartDate(), answer.getStartDate());
        assertEquals(booking.getEndDate(), answer.getEndDate());
        assertEquals(booking.getItem().getId(), answer.getItem().getId());
        assertEquals(booking.getBooker().getId(), answer.getBooker().getId());
    }

    @Test
    void getViewerIsOwner() {
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        Booking answer = bookingService.get(owner.getId(), booking.getId());

        assertEquals(booking.getId(), answer.getId());
        assertEquals(booking.getStatus(), answer.getStatus());
        assertEquals(booking.getStartDate(), answer.getStartDate());
        assertEquals(booking.getEndDate(), answer.getEndDate());
        assertEquals(booking.getItem().getId(), answer.getItem().getId());
        assertEquals(booking.getBooker().getId(), answer.getBooker().getId());
    }

    @Test
    void getViewerIsNotBookerOrOwner() {
        User random = new User(2, "random", "random@com");
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.get(random.getId(), booking.getId()));
    }

    @Test
    void get() {
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        Booking answer = bookingService.get(booking.getId());

        assertEquals(booking.getId(), answer.getId());
        assertEquals(booking.getStatus(), answer.getStatus());
        assertEquals(booking.getStartDate(), answer.getStartDate());
        assertEquals(booking.getEndDate(), answer.getEndDate());
        assertEquals(booking.getItem().getId(), answer.getItem().getId());
        assertEquals(booking.getBooker().getId(), answer.getBooker().getId());
    }

    @Test
    void getThrowsNotFoundException() {
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.get(booking.getId()));
    }

    @Test
    void updateByItemOwner() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Booking answer = assertDoesNotThrow(() -> bookingService.update(owner.getId(), true, booking.getId()));
        assertEquals(booking.getId(), answer.getId());
        assertEquals(BookingStatus.APPROVED, answer.getStatus());
    }

    @Test
    void updateNotByItemOwner() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(NotFoundException.class, () -> bookingService.update(booker.getId(), true, booking.getId()));
    }

    @Test
    void updateApprovedByItemOwner() {
        booking.setStatus(BookingStatus.APPROVED);
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(ValidationException.class, () -> bookingService.update(owner.getId(), true, booking.getId()));
    }

    @Test
    void getUserBookingsWithPagination() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findAllByBookerId(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByBookerIdOrderByEndDateDesc(Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        List<Booking> answer = assertDoesNotThrow(() ->
                bookingService.getUserBookings(booker.getId(), 0, 5, "ALL"));
        assertEquals(0, answer.size());
    }

    @Test
    void getUserBookingsWithoutPagination() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findAllByBookerId(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByBookerIdOrderByEndDateDesc(Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        List<Booking> answer = assertDoesNotThrow(() ->
                bookingService.getUserBookings(booker.getId(), null, null, "ALL"));
        assertEquals(0, answer.size());
    }

    @Test
    void getUserBookingsThrowsException() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findAllByBookerId(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByBookerIdOrderByEndDateDesc(Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () ->
                bookingService.getUserBookings(booker.getId(), null, 5, "ALL"));
    }

    @Test
    void getBookingsOfUserItemsWithPagination() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findAllByBookingItemOwnerId(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByBookingItemOwnerId(Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        List<Booking> answer = assertDoesNotThrow(() ->
                bookingService.getBookingsOfUserItems(owner.getId(), 0, 5, "ALL"));
        assertEquals(0, answer.size());
    }

    @Test
    void getBookingsOfUserItemsWithoutPagination() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findAllByBookingItemOwnerId(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByBookingItemOwnerId(Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        List<Booking> answer = assertDoesNotThrow(() ->
                bookingService.getBookingsOfUserItems(owner.getId(), null, null, "ALL"));
        assertEquals(0, answer.size());
    }

    @Test
    void getBookingsOfUserItemsThrowsException() {
        Mockito.when(userService.get(owner.getId()))
                .thenReturn(null);
        Mockito.when(bookingRepository.findAllByBookingItemOwnerId(Mockito.anyInt(), Mockito.any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findAllByBookingItemOwnerId(Mockito.anyInt()))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () ->
                bookingService.getBookingsOfUserItems(owner.getId(), null, 5, "ALL"));
    }
}
