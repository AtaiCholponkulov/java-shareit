package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceIntegrationTest {

    private final EntityManager em;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemRequestService service;

    @Test
    void add() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need look").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        ItemRequest dbItemRequest = em
                .createQuery("select ir from ItemRequest ir where ir.id = :id", ItemRequest.class)
                .setParameter("id", itemRequest.getId())
                .getSingleResult();
        assertThat(dbItemRequest.getId(), is(itemRequest.getId()));
        assertThat(dbItemRequest.getCreated(), is(itemRequest.getCreated()));
        assertThat(dbItemRequest.getDescription(), is(itemRequest.getDescription()));
        assertThat(dbItemRequest.getRequester().getName(), is(itemRequest.getRequester().getName()));
    }

    @Test
    void get() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need good").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        ItemRequest dbItemRequest = assertDoesNotThrow(() -> service.get(itemRequest.getId()));
        assertThat(dbItemRequest.getId(), is(itemRequest.getId()));
        assertThat(dbItemRequest.getCreated(), is(itemRequest.getCreated()));
        assertThat(dbItemRequest.getDescription(), is(itemRequest.getDescription()));
        assertThat(dbItemRequest.getRequester().getName(), is(itemRequest.getRequester().getName()));
    }

    @Test
    void getNotFound() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need food").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        assertThrows(NotFoundException.class, () -> service.get(10));
    }

    @Test
    void getUserRequests() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        User user2 = userService.add(new User(null, "user2", "user2@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need dog").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        Item item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .available(true)
                        .description("descr")
                        .requestId(itemRequest.getId())
                        .build(),
                user2.getId());
        em.detach(item);
        List<ItemRequestDto> result = service.getUserRequests(user1.getId());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(itemRequest.getId()));
        assertThat(result.get(0).getCreated(), is(itemRequest.getCreated()));
        assertThat(result.get(0).getDescription(), is(itemRequest.getDescription()));
        assertThat(result.get(0).getItems(), hasSize(1));
        assertThat(result.get(0).getItems().get(0).getName(), is(item.getName()));
        assertThat(result.get(0).getItems().get(0).getAvailable(), is(item.getAvailable()));
    }

    @Test
    void testGet() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        User user2 = userService.add(new User(null, "user2", "user2@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need fog").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        Item item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .available(true)
                        .description("descr")
                        .requestId(itemRequest.getId())
                        .build(),
                user2.getId());
        em.detach(item);
        ItemRequestDto result = service.get(user1.getId(), itemRequest.getId());
        assertThat(result, notNullValue());
        assertThat(result.getId(), is(itemRequest.getId()));
        assertThat(result.getCreated(), is(itemRequest.getCreated()));
        assertThat(result.getDescription(), is(itemRequest.getDescription()));
        assertThat(result.getItems(), hasSize(1));
        assertThat(result.getItems().get(0).getName(), is(item.getName()));
        assertThat(result.getItems().get(0).getAvailable(), is(item.getAvailable()));
    }

    @Test
    void getOthersRequestsThrowsPaginationException() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        User user2 = userService.add(new User(null, "user2", "user2@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need rogue").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        Item item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .available(true)
                        .description("descr")
                        .requestId(itemRequest.getId())
                        .build(),
                user2.getId());
        em.detach(item);
        assertThrows(ValidationException.class, () -> service.get(null, 1, user2.getId()));
    }

    @Test
    void getOthersRequestsNoPagination() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        User user2 = userService.add(new User(null, "user2", "user2@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need man").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        Item item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .available(true)
                        .description("descr")
                        .requestId(itemRequest.getId())
                        .build(),
                user2.getId());
        em.detach(item);
        List<ItemRequestDto> result = service.get(null, null, user2.getId());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(itemRequest.getId()));
        assertThat(result.get(0).getCreated(), is(itemRequest.getCreated()));
        assertThat(result.get(0).getDescription(), is(itemRequest.getDescription()));
        assertThat(result.get(0).getItems(), hasSize(1));
        assertThat(result.get(0).getItems().get(0).getName(), is(item.getName()));
        assertThat(result.get(0).getItems().get(0).getAvailable(), is(item.getAvailable()));
    }

    @Test
    void getOthersRequestsWithPagination() {
        User user1 = userService.add(new User(null, "user1", "user1@mail.com"));
        User user2 = userService.add(new User(null, "user2", "user2@mail.com"));
        ItemRequestDto itemRequestDto = ItemRequestDto.builder().description("need guy").build();
        ItemRequest itemRequest = service.add(user1.getId(), itemRequestDto);
        em.detach(itemRequest);
        Item item = itemService.add(
                ItemDto.builder()
                        .name("item")
                        .available(true)
                        .description("descr")
                        .requestId(itemRequest.getId())
                        .build(),
                user2.getId());
        em.detach(item);
        List<ItemRequestDto> result = service.get(0, 1, user2.getId());
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(itemRequest.getId()));
        assertThat(result.get(0).getCreated(), is(itemRequest.getCreated()));
        assertThat(result.get(0).getDescription(), is(itemRequest.getDescription()));
        assertThat(result.get(0).getItems(), hasSize(1));
        assertThat(result.get(0).getItems().get(0).getName(), is(item.getName()));
        assertThat(result.get(0).getItems().get(0).getAvailable(), is(item.getAvailable()));
    }
}