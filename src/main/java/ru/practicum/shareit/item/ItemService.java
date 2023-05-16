package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.mapper.Mapper.map;

@Service
@RequiredArgsConstructor
public class ItemService {

    @Qualifier("InMemoryItemStorage")
    private final ItemStorage itemStorage;
    private final UserService userService;

    public ItemDto add(ItemDto itemDto, int ownerId) {
        userService.get(ownerId);
        Item item = map(itemDto, ownerId);
        return map(itemStorage.add(item));
    }

    public ItemDto get(int itemId, int viewerId) {
        userService.get(viewerId);
        return map(itemStorage.get(itemId));
    }

    public List<ItemDto> getOwnerItems(int ownerId) {
        return map(itemStorage.getOwnerItems(ownerId));
    }

    public List<ItemDto> search(String word, int viewerId) {
        userService.get(viewerId);
        return word.isBlank() ? new ArrayList<>() : map(itemStorage.search(word));
    }

    public ItemDto update(int itemId, int ownerId, ItemDto itemDto) {
        userService.get(ownerId);
        Item item = map(itemId, ownerId, itemDto);
        return map(itemStorage.update(item));
    }
}
