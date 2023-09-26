package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.filter.BookingFilter;

import static ru.practicum.shareit.common.Header.X_SHARER_USER_ID;
import static ru.practicum.shareit.validator.Validator.validate;
import static ru.practicum.shareit.validator.Validator.validateState;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> add(@RequestHeader(name = X_SHARER_USER_ID) int bookerId,
									  @RequestBody BookingDtoIn bookingDtoIn) {
		validate(bookingDtoIn);
		return bookingClient.addBooking(bookerId, bookingDtoIn);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> get(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
									  @PathVariable int bookingId) {
		return bookingClient.getBooking(viewerId, bookingId);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> update(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
										 @RequestParam boolean approved,
										 @PathVariable int bookingId) {
		return bookingClient.updateBooking(viewerId, approved, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getUserBookings(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
												  @RequestParam(required = false) Integer from,
												  @RequestParam(required = false) Integer size,
												  @RequestParam(defaultValue = "ALL") String state) {
		BookingFilter filter = validateState(state);
		return bookingClient.getUserBookings(viewerId, from, size, filter);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getBookingsOfUserItems(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
														 @RequestParam(required = false) Integer from,
														 @RequestParam(required = false) Integer size,
														 @RequestParam(defaultValue = "ALL") String state) {
		BookingFilter filter = validateState(state);
		return bookingClient.getBookingsOfUserItems(viewerId, from, size, filter);
	}
}
