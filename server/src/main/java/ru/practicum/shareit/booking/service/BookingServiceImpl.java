package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.mapper.BookingMapper.map;
import static ru.practicum.shareit.validator.Validator.isForPagination;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public Booking add(int bookerId, BookingDtoIn bookingDtoIn) {
        User booker = getUser(bookerId);
        Item item = itemRepository.findById(bookingDtoIn.getItemId()).orElseThrow(() ->
                new NotFoundException("Такой вещи нет в базе id=" + bookingDtoIn.getItemId()));
        if (!item.getAvailable()) {
            throw new ValidationException("Вещь не доступна id=" + item.getId());
        }
        if (item.getOwner().getId() == bookerId) {
            throw new NotFoundException("Вещь не доступна id=" + item.getId());
        }
        Booking booking = map(
                bookingDtoIn.getStart(),
                bookingDtoIn.getEnd(),
                item,
                booker);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking get(int viewerId, int bookingId) {
        getUser(viewerId);
        Booking booking = get(bookingId);
        if (viewerId != booking.getBooker().getId() && viewerId != booking.getItem().getOwner().getId()) {
            throw new NotFoundException("У пользователя id=" + viewerId + " нет доступа к брони id=" + bookingId);
        }
        return booking;
    }

    @Override
    public Booking get(int bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() ->
                new NotFoundException("Такой брони нет id=" + bookingId));
    }

    @Transactional
    @Override
    public Booking update(int viewerId, boolean approved, int bookingId) {
        getUser(viewerId);
        Booking booking = this.get(bookingId);
        if (booking.getItem().getOwner().getId() != viewerId) {
            throw new NotFoundException("Пользователь id=" + viewerId +
                    " не является владельцем вещи id=" + booking.getItem().getId());
        }
        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new ValidationException("Бронь id=" + bookingId + " уже одобрена");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public List<Booking> getUserBookings(int viewerId, Integer from, Integer size, String state) {
        getUser(viewerId);
        LocalDateTime now = LocalDateTime.now();
        if (isForPagination(from, size)) {
            List<Booking> userBookings;
            Pageable page = PageRequest.of(0, from + size, Sort.by("endDate").descending());
            switch (state) {
                case "ALL":
                    userBookings = bookingRepository.findByBookerId(viewerId, page);
                    break;
                case "PAST":
                    userBookings = bookingRepository.findByBookerIdAndEndDateBefore(viewerId, now, page);
                    break;
                case "CURRENT":
                    userBookings = bookingRepository.findByBookerIdAndStartDateBeforeAndEndDateAfter(viewerId, now, now, page);
                    break;
                case "FUTURE":
                    userBookings = bookingRepository.findByBookerIdAndStartDateAfter(viewerId, now, page);
                    break;
                case "WAITING":
                case "REJECTED":
                case "APPROVED":
                    userBookings = bookingRepository.findByBookerIdAndStatus(viewerId, BookingStatus.valueOf(state), page);
                    break;
                default:
                    throw new ValidationException("Unknown state: " + state);
            }
            return userBookings.subList(from, userBookings.size());
        } else {
            switch (state) {
                case "ALL":
                    return bookingRepository.findByBookerIdOrderByEndDateDesc(viewerId);
                case "PAST":
                    return bookingRepository.findByBookerIdAndEndDateBeforeOrderByEndDateDesc(viewerId, now);
                case "CURRENT":
                    return bookingRepository.findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByEndDateDesc(viewerId, now, now);
                case "FUTURE":
                    return bookingRepository.findByBookerIdAndStartDateAfterOrderByEndDateDesc(viewerId, now);
                case "WAITING":
                case "REJECTED":
                case "APPROVED":
                    return bookingRepository.findByBookerIdAndStatusOrderByEndDateDesc(viewerId, BookingStatus.valueOf(state));
                default:
                    throw new ValidationException("Unknown state: " + state);
            }
        }
    }

    @Override
    public List<Booking> getBookingsOfUserItems(int viewerId, Integer from, Integer size, String state) {
        getUser(viewerId);
        LocalDateTime now = LocalDateTime.now();
        if (isForPagination(from, size)) {
            Pageable page = PageRequest.of(from, size);
            switch (state) {
                case "ALL":
                    return bookingRepository.findByOwnerId(viewerId, page);
                case "PAST":
                    return bookingRepository.findByOwnerIdPast(viewerId, now, page);
                case "CURRENT":
                    return bookingRepository.findByOwnerIdCurrent(viewerId, now, now, page);
                case "FUTURE":
                    return bookingRepository.findByOwnerIdFuture(viewerId, now, page);
                case "WAITING":
                case "REJECTED":
                case "APPROVED":
                    return bookingRepository.findByOwnerIdAndStatus(viewerId, BookingStatus.valueOf(state), page);
                default:
                    throw new ValidationException("Unknown state: " + state);
            }
        } else {
            switch (state) {
                case "ALL":
                    return bookingRepository.findByOwnerId(viewerId);
                case "PAST":
                    return bookingRepository.findByOwnerIdPast(viewerId, now);
                case "CURRENT":
                    return bookingRepository.findByOwnerIdCurrent(viewerId, now, now);
                case "FUTURE":
                    return bookingRepository.findByOwnerIdFuture(viewerId, now);
                case "WAITING":
                case "REJECTED":
                case "APPROVED":
                    return bookingRepository.findByOwnerIdAndStatus(viewerId, BookingStatus.valueOf(state));
                default:
                    throw new ValidationException("Unknown state: " + state);
            }
        }
    }

    private User getUser(int userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Такого пользователя нет в базе id=" + userId));
    }
}
