package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import static ru.practicum.shareit.common.Header.X_SHARER_USER_ID;
import static ru.practicum.shareit.validator.Validator.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemService;

    //-----------------------------------------------ITEM ENDPOINTS-----------------------------------------------------

    @PostMapping
    public ResponseEntity<Object> add(@RequestBody ItemDto itemDto,
                                      @RequestHeader(name = X_SHARER_USER_ID) int ownerId) {
        validateItem(itemDto);
        return itemService.addItem(itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(@PathVariable int itemId,
                                      @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemService.getItem(itemId, viewerId);
    }

    @GetMapping
    public ResponseEntity<Object> getViewerItems(@RequestHeader(name = X_SHARER_USER_ID) int viewerId,
                                                 @RequestParam(required = false) Integer from,
                                                 @RequestParam(required = false) Integer size) {
        return itemService.getViewerItems(viewerId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(required = false) Integer from,
                                         @RequestParam(required = false) Integer size,
                                         @RequestParam String text,
                                         @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemService.search(text, from, size, viewerId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable int itemId,
                                         @RequestHeader(name = X_SHARER_USER_ID) int ownerId,
                                         @RequestBody ItemDto itemDto) {
        validateUpdateItem(itemDto);
        return itemService.updateItem(itemId, ownerId, itemDto);
    }

    //----------------------------------------------COMMENT ENDPOINTS---------------------------------------------------

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> add(@RequestBody CommentDto commentDto,
                                      @RequestHeader(name = X_SHARER_USER_ID) int commentatorId,
                                      @PathVariable int itemId) {
        validate(commentDto);
        return itemService.addComment(commentDto, commentatorId, itemId);
    }
}
