package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByOwnerIdOrderById(Integer ownerId);

    List<Item> findByOwnerIdOrderById(Integer ownerId, Pageable pageable);

//    @Query("SELECT i " +
//            "FROM Item i " +
//            "WHERE i.available = true " +
//            "AND (LOWER(i.name) LIKE %?1%" +
//            "OR LOWER(i.description) LIKE %?1%)")//db COLLATE and CTYPE settings should be specific to support cyrillic
    List<Item> findAllByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(String name, String description);

//    @Query("SELECT i " +
//            "FROM Item i " +
//            "WHERE i.available = true " +
//            "AND (LOWER(i.name) LIKE %?1%" +
//            "OR LOWER(i.description) LIKE %?1%)")//db COLLATE and CTYPE settings should be specific to support cyrillic
    List<Item> findAllByNameOrDescriptionContainingIgnoreCaseAndAvailableTrue(String name, String description, Pageable pageable);

    List<Item> findByRequestId(Integer requestId);

    List<Item> findByRequestIdIn(Set<Integer> requestIds);
}
