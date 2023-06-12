package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static ru.practicum.shareit.item.mapper.CommentMapper.map;
import static ru.practicum.shareit.item.mapper.ItemMapper.map;
import static ru.practicum.shareit.validator.Validator.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private static final String X_SHARER_USER_ID = "X-Sharer-User-Id";

    //-----------------------------------------------ITEM ENDPOINTS-----------------------------------------------------

    @PostMapping
    public ItemDto add(@RequestBody ItemDto itemDto,
                       @RequestHeader(name = X_SHARER_USER_ID) int ownerId) {
        validateItem(itemDto);
        return ItemMapper.map(itemService.add(itemDto, ownerId));
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithBookingsAndComments get(@PathVariable int itemId,
                                              @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemService.get(itemId, viewerId);
    }

    @GetMapping
    public List<ItemDtoWithBookingsAndComments> getViewerItems(@RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return itemService.getViewerItems(viewerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text,
                                @RequestHeader(name = X_SHARER_USER_ID) int viewerId) {
        return map(itemService.search(text, viewerId));
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable int itemId,
                          @RequestHeader(name = X_SHARER_USER_ID) int ownerId,
                          @RequestBody ItemDto itemDto) {
        validateUpdateItem(itemDto);
        return ItemMapper.map(itemService.update(itemId, ownerId, itemDto));
    }

    //----------------------------------------------COMMENT ENDPOINTS---------------------------------------------------

    @PostMapping("/{itemId}/comment")
    public CommentDto add(@RequestBody CommentDto commentDto,
                          @RequestHeader(name = X_SHARER_USER_ID) int commentatorId,
                          @PathVariable int itemId) {
        validateComment(commentDto);
        return map(itemService.add(commentDto, commentatorId, itemId));
    }
}
