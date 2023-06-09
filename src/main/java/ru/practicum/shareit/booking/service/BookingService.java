package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ItemService itemService;
    private final UserService userService;

    @Transactional
    public Booking add(int bookerId, BookingDtoIn bookingDtoIn) {
        User booker = userService.get(bookerId);
        Item item = itemService.get(bookingDtoIn.getItemId());
        if (!item.getAvailable())
            throw new ValidationException("Вещь не доступна id=" + item.getId());
        if (item.getOwner().getId() == bookerId)
            throw new NotFoundException("Вещь не доступна id=" + item.getId());
        Booking booking = map(
                bookingDtoIn.getStart(),
                bookingDtoIn.getEnd(),
                item,
                booker);
        return bookingRepository.save(booking);
    }

    public Booking get(int viewerId, int bookingId) {
        userService.get(viewerId);
        Booking booking = get(bookingId);
        if (viewerId != booking.getBooker().getId() && viewerId != booking.getItem().getOwner().getId())
            throw new NotFoundException("У пользователя id=" + viewerId + " нет доступа к брони id=" + bookingId);
        return booking;
    }

    public Booking get(int bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Такой брони нет id=" + bookingId));
    }

    @Transactional
    public Booking update(int viewerId, boolean approved, int bookingId) {
        userService.get(viewerId);
        Booking booking = this.get(bookingId);
        if (booking.getItem().getOwner().getId() != viewerId)
            throw new NotFoundException("Пользователь id=" + viewerId +
                    " не является владельцем вещи id=" + booking.getItem().getId());
        if (booking.getStatus().equals(BookingStatus.APPROVED))
            throw new ValidationException("Бронь id=" + bookingId + " уже одобрена");
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(int viewerId, String state) {
        userService.get(viewerId);
        List<Booking> userBookings = bookingRepository.findAllByBookerIdOrderByStartDateDesc(viewerId);
        return filterByState(userBookings, state);
    }

    public List<Booking> getBookingsOfUserItems(int viewerId, String state) {
        userService.get(viewerId);
        List<Booking> bookingsOfUserItems = bookingRepository.findAllByBookingItemOwnerId(viewerId);
        return filterByState(bookingsOfUserItems, state);
    }

    private List<Booking> filterByState(List<Booking> bookings, String state) {
        LocalDateTime now = LocalDateTime.now();
        Predicate<Booking> bookingPredicate;
        switch (state) {
            case "ALL":
                return bookings;
            case "PAST":
                bookingPredicate = booking -> now.isAfter(booking.getEndDate());
                break;
            case "CURRENT":
                bookingPredicate = booking -> now.isAfter(booking.getStartDate()) && now.isBefore(booking.getEndDate());
                break;
            case "FUTURE":
                bookingPredicate = booking -> now.isBefore(booking.getStartDate());
                break;
            case "WAITING":
                bookingPredicate = booking -> booking.getStatus().equals(BookingStatus.WAITING);
                break;
            case "REJECTED":
                bookingPredicate = booking -> booking.getStatus().equals(BookingStatus.REJECTED);
                break;
            case "APPROVED":
                bookingPredicate = booking -> booking.getStatus().equals(BookingStatus.APPROVED);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }
        return bookings.stream()
                .filter(bookingPredicate)
                .collect(Collectors.toList());
    }
}
