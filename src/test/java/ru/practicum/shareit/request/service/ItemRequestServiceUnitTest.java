package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.request.mapper.ItemRequestMapper.map;

class ItemRequestServiceUnitTest {

    @Mock
    private final UserService userService = Mockito.mock(UserServiceImpl.class);
    private final ItemRepository itemRepository = Mockito.mock(ItemRepository.class);
    private final ItemRequestRepository itemRequestRepository = Mockito.mock(ItemRequestRepository.class);
    private final ItemRequestService itemRequestService = new ItemRequestServiceImpl(userService, itemRepository, itemRequestRepository);
    private static User requester;
    private static ItemRequestDto itemRequestDto;
    private static ItemRequest itemRequest;

    @BeforeAll
    static void beforeAll() {
        requester = new User(0, "requester", "requester@com");
        itemRequestDto = ItemRequestDto.builder()
                .description("need item")
                .build();
        itemRequest = map(itemRequestDto, requester);
    }

    @Test
    void add() {
        Mockito.when(userService.get(requester.getId()))
                .thenReturn(requester);
        Mockito.when(itemRequestRepository.save(Mockito.any(ItemRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ItemRequest answer = itemRequestService.add(requester.getId(), itemRequestDto);
        assertEquals(itemRequest.getId(), answer.getId());
        assertEquals(itemRequest.getDescription(), answer.getDescription());
        assertEquals(itemRequest.getRequester().getId(), answer.getRequester().getId());
    }

    @Test
    void get() {
        Mockito.when(itemRequestRepository.findById(0))
                .thenReturn(Optional.of(itemRequest));

        ItemRequest answer = itemRequestService.get(0);
        assertEquals(itemRequest.getCreated(), answer.getCreated());
        assertEquals(itemRequest.getRequester().getId(), answer.getRequester().getId());
        assertEquals(itemRequest.getDescription(), answer.getDescription());
    }

    @Test
    void getThrowsNotFoundException() {
        Mockito.when(itemRequestRepository.findById(0))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.get(0));
    }

    @Test
    void getUserRequests() {
        Mockito.when(userService.get(Mockito.anyInt()))
                .thenReturn(null);
        Mockito.when(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(0))
                .thenReturn(List.of(itemRequest));

        List<ItemRequestDto> answer = itemRequestService.getUserRequests(0);
        ItemRequestDto answerDto = answer.get(0);
        assertEquals(1, answer.size());
        assertEquals(itemRequest.getDescription(), answerDto.getDescription());
    }

    @Test
    void getItemRequestWithItems() {
        User owner = new User(1, "owner", "owner@com");
        Item item = new Item(0, "item", "description", true, owner, itemRequest);
        Mockito.when(userService.get(Mockito.anyInt()))
                .thenReturn(null);
        Mockito.when(itemRepository.findAllByRequestId(Mockito.anyInt()))
                .thenReturn(List.of(item));
        Mockito.when(itemRequestRepository.findById(Mockito.anyInt()))
                .thenReturn(Optional.of(itemRequest));

        ItemRequestDto answer = itemRequestService.get(3, 10);
        assertEquals(itemRequest.getDescription(), answer.getDescription());
        assertEquals(1, answer.getItems().size());
        assertEquals(item.getId(), answer.getItems().get(0).getId());
        assertEquals(item.getRequest().getId(), answer.getItems().get(0).getRequestId());
        assertEquals(item.getDescription(), answer.getItems().get(0).getDescription());
    }

    @Test
    void getRequestsNotOfViewerWithPagination() {
        Mockito.when(userService.get(requester.getId()))
                .thenReturn(null);
        Mockito.when(itemRequestRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());
        Mockito.when(itemRequestRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> answer = assertDoesNotThrow(() ->
                itemRequestService.get(0, 5, requester.getId()));
        assertEquals(0, answer.size());
    }

    @Test
    void getRequestsNotOfViewerWithoutPagination() {
        Mockito.when(userService.get(requester.getId()))
                .thenReturn(null);
        Mockito.when(itemRequestRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());
        Mockito.when(itemRequestRepository.findAll())
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> answer = assertDoesNotThrow(() ->
                itemRequestService.get(null, null, requester.getId()));
        assertEquals(0, answer.size());
    }

    @Test
    void getRequestsNotOfViewerWithPaginationThrowsException() {
        Mockito.when(userService.get(requester.getId()))
                .thenReturn(null);
        Mockito.when(itemRequestRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(Page.empty());
        Mockito.when(itemRequestRepository.findAll())
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () ->
                itemRequestService.get(0, null, requester.getId()));
    }
}
