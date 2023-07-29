package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static ru.practicum.shareit.request.mapper.ItemRequestMapper.map;
import static ru.practicum.shareit.validator.Validator.validate;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemRequestDto add(@RequestHeader(name = X_SHARER_USER_ID) int requesterId,
                              @RequestBody ItemRequestDto itemRequest) {
        validate(itemRequest);
        return map(itemRequestService.add(requesterId, itemRequest));
    }

    @GetMapping
    public List<ItemRequestDto> getUserRequests(@RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemRequestService.getUserRequests(viewerId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto get(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
                              @PathVariable int requestId) {
        return itemRequestService.get(viewerId, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> get(@RequestParam(required = false) Integer from,
                                    @RequestParam(required = false) Integer size,
                                    @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemRequestService.get(from, size, viewerId);
    }
}
