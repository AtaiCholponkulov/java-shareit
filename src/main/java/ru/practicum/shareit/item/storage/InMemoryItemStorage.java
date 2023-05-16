package ru.practicum.shareit.item.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.validator.Validator.validateItem;
import static ru.practicum.shareit.validator.Validator.validateUpdateItem;

@Component("InMemoryItemStorage")
@Slf4j
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Integer, Item> db = new HashMap<>();
    private final Map<Integer, List<Integer>> ownerItemsIds = new HashMap<>();
    private int id = 1;

    private int getId() {
        return id++;
    }

    @Override
    public Item add(Item item) {
        validateItem(item);
        item.setId(getId());
        db.put(item.getId(), item);
        ownerItemsIds.compute(item.getOwnerId(), (ownerId, ownerItems) -> {
            if (ownerItems == null) ownerItems = new ArrayList<>();
            ownerItems.add(item.getId());
            return ownerItems;
        });
        log.info("Добавлен новый предмет id={}", item.getId());
        return item;
    }

    @Override
    public Item get(int itemId) {
        return Optional.ofNullable(db.get(itemId))
                .orElseThrow(() -> new NotFoundException("Такого предмета нет в базе id=" + itemId));
    }

    @Override
    public List<Item> getOwnerItems(int ownerId) {
        return ownerItemsIds.getOrDefault(ownerId, new ArrayList<>())
                .stream()
                .map(db::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String word) {
        return db.values().stream()
                .filter(item -> item.getAvailable()
                        && (item.getName().toLowerCase().contains(word.toLowerCase())
                        || item.getDescription().toLowerCase().contains(word.toLowerCase())))
                .collect(Collectors.toList());
    }

    @Override
    public Item update(Item item) {
        validateUpdateItem(item);
        Item dbItem = this.get(item.getId());
        if (!ownerItemsIds.getOrDefault(item.getOwnerId(), new ArrayList<>()).contains(item.getId()))
            throw new NotFoundException("Пользователь с id=" + item.getOwnerId() +
                    " не является владельцем предмета с id=" + item.getId());
        dbItem.update(item);
        log.info("Обновлен предмет id={}", dbItem.getId());
        return dbItem;
    }
}
