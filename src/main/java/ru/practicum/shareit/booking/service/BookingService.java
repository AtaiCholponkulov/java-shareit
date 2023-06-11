package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Service
public interface BookingService {
    @Transactional
    Booking add(int bookerId, BookingDtoIn bookingDtoIn);

    Booking get(int viewerId, int bookingId);

    Booking get(int bookingId);

    @Transactional
    Booking update(int viewerId, boolean approved, int bookingId);

    List<Booking> getUserBookings(int viewerId, String state);

    List<Booking> getBookingsOfUserItems(int viewerId, String state);
}
