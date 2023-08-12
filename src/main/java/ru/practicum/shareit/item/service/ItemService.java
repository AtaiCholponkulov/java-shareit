package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Service
public interface ItemService {
    @Transactional
    Comment add(CommentDto commentDto, int commentatorId, int itemId);

    @Transactional
    Item add(ItemDto itemDto, int ownerId);

    ItemDtoWithBookingsAndComments get(int itemId, int viewerId);

    Item get(int itemId);

    List<ItemDtoWithBookingsAndComments> getViewerItems(int viewerId, Integer from, Integer size);

    List<Item> search(String word, Integer from, Integer size, int viewerId);

    @Transactional
    Item update(int itemId, int ownerId, ItemDto itemDto);
}
