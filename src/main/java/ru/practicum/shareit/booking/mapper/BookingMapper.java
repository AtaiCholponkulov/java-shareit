package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.dto.ItemFieldBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BookingMapper {

    public static Booking map(LocalDateTime startDate, LocalDateTime endDate, Item item, User booker) {
        return new Booking(
                null,
                startDate,
                endDate,
                item,
                booker,
                BookingStatus.WAITING);
    }

    public static BookingDtoOut map(Booking booking) {
        return BookingDtoOut.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }

    public static ItemFieldBookingDto mapToItemField(Booking booking) {
        return ItemFieldBookingDto.builder()
                .id(booking.getId())
                .start(booking.getStartDate())
                .end(booking.getEndDate())
                .item(booking.getItem())
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus())
                .build();
    }

    public static List<BookingDtoOut> map(List<Booking> bookings) {
        return bookings.stream()
                .map(BookingMapper::map)
//                .sorted((booking1, booking2) -> booking2.getEnd().compareTo(booking1.getEnd()))
                .collect(Collectors.toList());
    }
}
