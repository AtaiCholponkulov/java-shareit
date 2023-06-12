package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findAllByOwnerIdOrderById(Integer ownerId);

    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE %?1%" +
            "OR LOWER(i.description) LIKE %?1%)")//db COLLATE and CTYPE settings should be specific to support cyrillic
    List<Item> findAllAvailableItemsByWord(String word);
}
