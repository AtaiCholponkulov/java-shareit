package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item add(Item item);

    Item get(int itemId);

    List<Item> getOwnerItems(int ownerId);

    List<Item> search(String word);

    Item update(Item item);
}
