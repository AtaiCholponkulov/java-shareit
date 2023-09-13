package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static ru.practicum.shareit.common.Header.X_SHARER_USER_ID;
import static ru.practicum.shareit.validator.Validator.validate;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> add(@RequestHeader(name = X_SHARER_USER_ID) int requesterId,
                                      @RequestBody ItemRequestDto itemRequest) {
        validate(itemRequest);
        return itemRequestClient.addRequest(requesterId, itemRequest);
    }

    @GetMapping
    public ResponseEntity<Object> getUserRequests(@RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemRequestClient.getUserRequests(viewerId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> get(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
                                      @PathVariable int requestId) {
        return itemRequestClient.get(viewerId, requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> get(@RequestParam(required = false) Integer from,
                                      @RequestParam(required = false) Integer size,
                                      @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemRequestClient.get(from, size, viewerId);
    }
}
