package ru.practicum.shareit.booking.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

import static ru.practicum.shareit.validator.Validator.validatePaginationParams;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }


    public ResponseEntity<Object> addBooking(int bookerId, BookingDtoIn bookingDtoIn) {
        return post("", bookerId, bookingDtoIn);
    }

    public ResponseEntity<Object> getBooking(int viewerId, int bookingId) {
        return get("/" + bookingId, viewerId);
    }

    public ResponseEntity<Object> updateBooking(int viewerId, boolean approved, int bookingId) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );
        return patch("/" + bookingId + "?approved={approved}", viewerId, parameters, null);
    }

    public ResponseEntity<Object> getUserBookings(int viewerId, Integer from, Integer size, String state) {
        Map<String, Object> parameters;
        if (validatePaginationParams(from, size)) {
             parameters = Map.of(
                    "state", state,
                    "from", from,
                    "size", size
            );
            return get("?state={state}&from={from}&size={size}", viewerId, parameters);
        }
        parameters = Map.of("state", state);
        return get("?state={state}", viewerId, parameters);
    }

    public ResponseEntity<Object> getBookingsOfUserItems(int viewerId, Integer from, Integer size, String state) {
        Map<String, Object> parameters;
        if (validatePaginationParams(from, size)) {
             parameters = Map.of(
                    "state", state,
                    "from", from,
                    "size", size
            );
            return get("/owner?state={state}&from={from}&size={size}", viewerId, parameters);
        }
        parameters = Map.of("state", state);
        return get("/owner?state={state}", viewerId, parameters);
    }
}
