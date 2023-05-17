package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ItemDto add(@RequestBody ItemDto item, @RequestHeader(name = X_SHARER_USER_ID) int ownerId) {
        return itemService.add(item, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable int itemId, @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemService.get(itemId, viewerId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(name = X_SHARER_USER_ID) int ownerId) {
        return itemService.getOwnerItems(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text, @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemService.search(text, viewerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable int itemId, @RequestHeader(name = X_SHARER_USER_ID) int ownerId, @RequestBody ItemDto item) {
        return itemService.update(itemId, ownerId, item);
    }
}
