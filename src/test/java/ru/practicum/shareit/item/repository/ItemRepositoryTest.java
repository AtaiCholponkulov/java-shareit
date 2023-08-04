package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRepositoryTest {

    private final TestEntityManager em;
    private final ItemRepository itemRepository;
    private User user1;
    private User user2;
    private User user3;
    private ItemRequest itemRequest1;
    private ItemRequest itemRequest3;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    public void beforeEach() {
        user1 = new User(null, "user1", "user1@com");
        user2 = new User(null, "user2", "user2@com");
        user3 = new User(null, "user3", "user3@com");
        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        itemRequest1 = new ItemRequest(null, "need item1", user3, LocalDateTime.now());
        ItemRequest itemRequest2 = new ItemRequest(null, "need item2", user1, LocalDateTime.now());
        itemRequest3 = new ItemRequest(null, "need item3", user2, LocalDateTime.now());
        em.persist(itemRequest1);
        em.persist(itemRequest2);
        em.persist(itemRequest3);
        item1 = new Item(null, "item1", "descr", true, user2, itemRequest1);
        item2 = new Item(null, "item2", "descr", false, user3, itemRequest2);
        item3 = new Item(null, "item3", "descr", true, user3, itemRequest3);
        itemRepository.findAll().forEach(em::remove);
        itemRepository.saveAll(List.of(item1, item2, item3));
        em.clear();
        em.flush();
        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
        assertNotNull(user3.getId());
        assertNotNull(itemRequest1.getId());
        assertNotNull(itemRequest2.getId());
        assertNotNull(itemRequest3.getId());
        assertNotNull(item1.getId());
        assertNotNull(item2.getId());
        assertNotNull(item3.getId());
    }

    @Test
    void findAllByOwnerIdOrderByIdNoPagination() {
        List<Item> user1Items = itemRepository.findByOwnerIdOrderById(user1.getId());
        assertEquals(0, user1Items.size());

        List<Item> user2Items = itemRepository.findByOwnerIdOrderById(user2.getId());
        assertEquals(1, user2Items.size());
        assertEquals(item1.getId(), user2Items.get(0).getId());
        assertEquals(item1.getName(), user2Items.get(0).getName());
        assertEquals(item1.getOwner().getName(), user2Items.get(0).getOwner().getName());

        List<Item> user3Items = itemRepository.findByOwnerIdOrderById(user3.getId());
        assertEquals(2, user3Items.size());
        assertEquals(item2.getId(), user3Items.get(0).getId());
        assertEquals(item2.getName(), user3Items.get(0).getName());
        assertEquals(item2.getOwner().getName(), user3Items.get(0).getOwner().getName());
        assertEquals(item3.getId(), user3Items.get(1).getId());
        assertEquals(item3.getName(), user3Items.get(1).getName());
        assertEquals(item3.getOwner().getName(), user3Items.get(1).getOwner().getName());

    }

    @Test
    void findAllByOwnerIdOrderByIdWithPagination() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");

        List<Item> user3Items = itemRepository.findByOwnerIdOrderById(user3.getId(), PageRequest.of(0, 1, sort));
        assertEquals(1, user3Items.size());
        assertEquals(item2.getId(), user3Items.get(0).getId());
        assertEquals(item2.getDescription(), user3Items.get(0).getDescription());
        assertEquals(item2.getOwner().getName(), user3Items.get(0).getOwner().getName());

        user3Items = itemRepository.findByOwnerIdOrderById(user3.getId(), PageRequest.of(1, 1, sort));
        assertEquals(1, user3Items.size());
        assertEquals(item3.getId(), user3Items.get(0).getId());
        assertEquals(item3.getDescription(), user3Items.get(0).getDescription());
        assertEquals(item3.getOwner().getName(), user3Items.get(0).getOwner().getName());

    }

    @Test
    void findAllAvailableItemsByWordWithPagination() {
        Pageable page = PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "id"));

        List<Item> searchResults = itemRepository.findAvailableByWord("item", page)
                .stream()
                .sorted(Comparator.comparingInt(Item::getId))
                .collect(Collectors.toList());
        assertEquals(1, searchResults.size());
        assertEquals(item3.getId(), searchResults.get(0).getId());
        assertEquals(item3.getDescription(), searchResults.get(0).getDescription());
        assertEquals(item3.getOwner().getName(), searchResults.get(0).getOwner().getName());
    }

    @Test
    void findAllAvailableItemsByWordNoPagination() {
        List<Item> searchResults = itemRepository.findAvailableByWord("item")
                .stream()
                .sorted(Comparator.comparingInt(Item::getId))
                .collect(Collectors.toList());
        assertEquals(2, searchResults.size());
        assertEquals(item1.getId(), searchResults.get(0).getId());
        assertEquals(item1.getDescription(), searchResults.get(0).getDescription());
        assertEquals(item1.getOwner().getName(), searchResults.get(0).getOwner().getName());
        assertEquals(item3.getId(), searchResults.get(1).getId());
        assertEquals(item3.getDescription(), searchResults.get(1).getDescription());
        assertEquals(item3.getOwner().getName(), searchResults.get(1).getOwner().getName());
    }

    @Test
    void findAllByRequestId() {
        List<Item> items = itemRepository.findByRequestId(itemRequest1.getId());
        assertEquals(1, items.size());
        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item1.getAvailable(), items.get(0).getAvailable());
        assertEquals(item1.getName(), items.get(0).getName());
        assertEquals(item1.getRequest().getDescription(), items.get(0).getRequest().getDescription());
        assertEquals(item1.getOwner().getName(), items.get(0).getOwner().getName());
    }

    @Test
    void findAllByRequestIdIn() {
        List<Item> items = itemRepository.findByRequestIdIn(Set.of(itemRequest1.getId(), itemRequest3.getId()))
                .stream()
                .sorted(Comparator.comparingInt(Item::getId))
                .collect(Collectors.toList());
        assertEquals(2, items.size());
        assertEquals(item1.getId(), items.get(0).getId());
        assertEquals(item1.getAvailable(), items.get(0).getAvailable());
        assertEquals(item1.getName(), items.get(0).getName());
        assertEquals(item1.getRequest().getDescription(), items.get(0).getRequest().getDescription());
        assertEquals(item1.getOwner().getName(), items.get(0).getOwner().getName());
        assertEquals(item3.getId(), items.get(1).getId());
        assertEquals(item3.getAvailable(), items.get(1).getAvailable());
        assertEquals(item3.getName(), items.get(1).getName());
        assertEquals(item3.getRequest().getDescription(), items.get(1).getRequest().getDescription());
        assertEquals(item3.getOwner().getName(), items.get(1).getOwner().getName());
    }
}