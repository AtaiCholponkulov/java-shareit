package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

import static ru.practicum.shareit.booking.mapper.BookingMapper.map;
import static ru.practicum.shareit.common.Header.X_SHARER_USER_ID;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDtoOut add(@RequestHeader(name = X_SHARER_USER_ID) int bookerId,
                             @RequestBody BookingDtoIn bookingDtoIn) {
        return map(bookingService.add(bookerId, bookingDtoIn));
    }

    @GetMapping("/{bookingId}")
    public BookingDtoOut get(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
                             @PathVariable int bookingId) {
        return map(bookingService.get(viewerId, bookingId));
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoOut update(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
                                @RequestParam boolean approved,
                                @PathVariable int bookingId) {
        return map(bookingService.update(viewerId, approved, bookingId));
    }

    @GetMapping
    public List<BookingDtoOut> getUserBookings(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
                                               @RequestParam(required = false) Integer from,
                                               @RequestParam(required = false) Integer size,
                                               @RequestParam(defaultValue = "ALL") String state) {
        return map(bookingService.getUserBookings(viewerId, from, size, state));
    }

    @GetMapping("/owner")
    public List<BookingDtoOut> getBookingsOfUserItems(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
                                                      @RequestParam(required = false) Integer from,
                                                      @RequestParam(required = false) Integer size,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        return map(bookingService.getBookingsOfUserItems(viewerId, from, size, state));
    }
}
