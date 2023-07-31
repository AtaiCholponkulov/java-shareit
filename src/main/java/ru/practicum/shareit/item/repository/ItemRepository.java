package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findAllByOwnerIdOrderById(Integer ownerId);

    List<Item> findAllByOwnerIdOrderById(Integer ownerId, Pageable pageable);

    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE %?1%" +
            "OR LOWER(i.description) LIKE %?1%)")//db COLLATE and CTYPE settings should be specific to support cyrillic
    List<Item> findAllAvailableItemsByWord(String word);

    @Query("SELECT i " +
            "FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE %?1%" +
            "OR LOWER(i.description) LIKE %?1%)")//db COLLATE and CTYPE settings should be specific to support cyrillic
    List<Item> findAllAvailableItemsByWord(String word, Pageable pageable);

    List<Item> findAllByRequestId(Integer requestId);

    List<Item> findAllByRequestIdIn(Set<Integer> requestIds);
}
