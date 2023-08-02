package ru.practicum.shareit.item.service;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.exception.model.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class ItemServiceUnitTest {

    private final ItemRepository itemRepository = Mockito.mock(ItemRepository.class);
    private final BookingRepository bookingRepository = Mockito.mock(BookingRepository.class);
    private final UserService userService = Mockito.mock(UserServiceImpl.class);
    private final CommentRepository commentRepository = Mockito.mock(CommentRepository.class);
    private final ItemRequestService itemRequestService = Mockito.mock(ItemRequestServiceImpl.class);
    private final ItemService itemService = new ItemServiceImpl(itemRepository,
            bookingRepository,
            userService,
            commentRepository,
            itemRequestService);
    private static User requestOwner;
    private static ItemRequest itemRequest;
    private static User itemOwner;
    private static Item item;
    private static LocalDateTime now;

    @BeforeAll
    static void beforeAll() {
        now = LocalDateTime.now();
        requestOwner = new User(0, "requester", "requester@com");
        itemRequest = new ItemRequest(0, "itemRequest", requestOwner, now.minusMonths(1));
        itemOwner = new User(1, "owner", "owner@com");
        item = new Item(0, "item", "itemDescription", true, itemOwner, itemRequest);
    }

    @Test
    void addCommentBookingStatusApproved() {
        CommentDto commentDto = CommentDto.builder()
                .id(0)
                .text("comment")
                .authorName("author")
                .build();
        Booking booking = new Booking(0,
                now.minusDays(2),
                now.minusDays(1),
                item,
                requestOwner,
                BookingStatus.APPROVED);
        int commentatorId = requestOwner.getId();

        Mockito.when(userService.get(commentatorId))
                .thenReturn(requestOwner);
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemIdAndStatus(item.getId(), BookingStatus.APPROVED))
                .thenReturn(List.of(booking));
        Mockito.when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Comment answerComment = assertDoesNotThrow(() -> itemService.add(commentDto, commentatorId, item.getId()));
        assertEquals(commentDto.getId(), answerComment.getId());
        assertEquals(commentatorId, answerComment.getAuthor().getId());
        assertEquals(item.getId(), answerComment.getItem().getId());
    }

    @Test
    void addCommentBookingStatusRejected() {
        CommentDto commentDto = CommentDto.builder()
                .id(0)
                .text("comment")
                .authorName("author")
                .build();
        Booking booking = new Booking(0,
                now.minusDays(2),
                now.minusDays(1),
                item,
                requestOwner,
                BookingStatus.REJECTED);
        int commentatorId = requestOwner.getId();

        Mockito.when(userService.get(commentatorId))
                .thenReturn(requestOwner);
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemIdAndStatus(item.getId(), BookingStatus.APPROVED))
                .thenReturn(List.of(booking));
        Mockito.when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(ValidationException.class, () -> itemService.add(commentDto, commentatorId, item.getId()));
    }

    @Test
    void addCommentEmptyBookingList() {
        CommentDto commentDto = CommentDto.builder()
                .id(0)
                .text("comment")
                .authorName("author")
                .build();
        int commentatorId = requestOwner.getId();

        Mockito.when(userService.get(commentatorId))
                .thenReturn(requestOwner);
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findAllByItemIdAndStatus(item.getId(), BookingStatus.APPROVED))
                .thenReturn(Collections.emptyList());
        Mockito.when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(ValidationException.class, () -> itemService.add(commentDto, commentatorId, item.getId()));
    }

    @Test
    void addItemWithoutRequest() {
        ItemDto itemDto = ItemDto.builder()
                .id(0)
                .name(item.getName())
                .description(item.getDescription())
                .available(true)
                .build();
        Mockito.when(userService.get(itemOwner.getId()))
                .thenReturn(itemOwner);
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Item answerItem = itemService.add(itemDto, itemOwner.getId());
        Mockito.verifyNoInteractions(itemRequestService);
        assertEquals(itemDto.getId(), answerItem.getId());
        assertNull(answerItem.getRequest());
        assertEquals(item.getName(), answerItem.getName());
        assertEquals(item.getDescription(), answerItem.getDescription());
        assertEquals(item.getOwner().getId(), answerItem.getOwner().getId());
    }

    @Test
    void addItemWithRequest() {
        ItemDto itemDto = ItemDto.builder()
                .id(0)
                .name(item.getName())
                .description(item.getDescription())
                .available(true)
                .requestId(itemRequest.getId())
                .build();
        Mockito.when(userService.get(itemOwner.getId()))
                .thenReturn(itemOwner);
        Mockito.when(itemRequestService.get(itemRequest.getId()))
                .thenReturn(itemRequest);
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Item answerItem = itemService.add(itemDto, itemOwner.getId());
        assertEquals(itemDto.getId(), answerItem.getId());
        assertEquals(itemRequest.getId(), answerItem.getRequest().getId());
        assertEquals(item.getName(), answerItem.getName());
        assertEquals(item.getDescription(), answerItem.getDescription());
        assertEquals(item.getOwner().getId(), answerItem.getOwner().getId());
    }

    @Test
    void getItemWithBookingsAndComments() {
        Mockito.when(userService.get(item.getId()))
                .thenReturn(null);
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findAllByItemIdOrderByCreatedDesc(item.getId()))
                .thenReturn(Collections.emptyList());
        Mockito.when(bookingRepository.findPrevByItemIdAndStatus(
                anyInt(),
                any(BookingStatus.class),
                any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));
        Mockito.when(bookingRepository.findNextByItemIdAndStatus(
                anyInt(),
                any(BookingStatus.class),
                any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));

        ItemDtoWithBookingsAndComments answer = itemService.get(item.getId(), itemOwner.getId());
        assertEquals(item.getId(), answer.getId());
        assertEquals(0, answer.getComments().size());
        assertEquals(item.getName(), answer.getName());
        assertEquals(item.getDescription(), answer.getDescription());
    }

    @Test
    void getItem() {
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        Item answerItem = itemService.get(0);
        User answerOwner = answerItem.getOwner();
        ItemRequest answerItemRequest = answerItem.getRequest();
        User answerRequester = answerItemRequest.getRequester();

        assertEquals(item.getId(), answerItem.getId());
        assertEquals(item.getName(), answerItem.getName());
        assertEquals(item.getDescription(), answerItem.getDescription());
        assertEquals(item.getAvailable(), answerItem.getAvailable());
        assertEquals(itemOwner.getId(), answerOwner.getId());
        assertEquals(itemOwner.getName(), answerOwner.getName());
        assertEquals(itemOwner.getEmail(), answerOwner.getEmail());
        assertEquals(itemRequest.getId(), answerItemRequest.getId());
        assertEquals(itemRequest.getDescription(), answerItemRequest.getDescription());
        assertEquals(itemRequest.getCreated(), answerItemRequest.getCreated());
        assertEquals(requestOwner.getId(), answerRequester.getId());
        assertEquals(requestOwner.getName(), answerRequester.getName());
        assertEquals(requestOwner.getEmail(), answerRequester.getEmail());
    }

    @Test
    void getItemThrowsException() {
        Mockito.when(itemRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.get(1));
    }

    @Test
    void getEmptyViewerItemsWithoutPagination() {
        Mockito.when(userService.get(anyInt()))
                .thenReturn(null);
        Mockito.when(itemRepository.findAllByOwnerIdOrderById(anyInt(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(itemRepository.findAllByOwnerIdOrderById(anyInt()))
                .thenReturn(Collections.emptyList());

        assertEquals(0, itemService.getViewerItems(itemOwner.getId(), null, null).size());
        Mockito.verify(itemRepository, Mockito.times(0))
                .findAllByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByOwnerIdOrderById(anyInt());
    }

    @Test
    void getViewerItemsWithoutPagination() {
        Comment comment = new Comment(0, "comment", item, requestOwner, now);
        Booking booking = new Booking(0,
                now.minusDays(2),
                now.minusDays(1),
                item,
                requestOwner,
                BookingStatus.APPROVED);
        Mockito.when(userService.get(anyInt()))
                .thenReturn(null);
        Mockito.when(itemRepository.findAllByOwnerIdOrderById(anyInt()))
                .thenReturn(List.of(item));
        Mockito.when(commentRepository.findAllByItemIdIn(Mockito.anySet()))
                .thenReturn(List.of(comment));
        Mockito.when(bookingRepository.findPrevByItemIdAndStatus(
                anyInt(),
                any(BookingStatus.class),
                any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new SliceImpl<>(List.of(booking)));
        Mockito.when(bookingRepository.findNextByItemIdAndStatus(
                anyInt(),
                any(BookingStatus.class),
                any(LocalDateTime.class),
                any(Pageable.class))).thenReturn(new SliceImpl<>(Collections.emptyList()));

        List<ItemDtoWithBookingsAndComments> result = itemService.getViewerItems(itemOwner.getId(), null, null);
        Mockito.verify(itemRepository, Mockito.times(0))
                .findAllByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByOwnerIdOrderById(anyInt());
        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getId(), Matchers.is(item.getId()));
        assertThat(result.get(0).getName(), Matchers.is(item.getName()));
        assertThat(result.get(0).getAvailable(), Matchers.is(item.getAvailable()));
        assertThat(result.get(0).getNextBooking(), Matchers.nullValue());
        assertThat(result.get(0).getLastBooking(), Matchers.notNullValue());
        assertThat(result.get(0).getLastBooking().getStart(), Matchers.is(booking.getStartDate()));
        assertThat(result.get(0).getLastBooking().getEnd(), Matchers.is(booking.getEndDate()));
        assertThat(result.get(0).getComments(), Matchers.hasSize(1));
        assertThat(result.get(0).getComments().get(0).getText(), Matchers.is(comment.getText()));
    }

    @Test
    void getViewerItemsWithPagination() {
        Mockito.when(userService.get(anyInt()))
                .thenReturn(null);
        Mockito.when(itemRepository.findAllByOwnerIdOrderById(anyInt(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(itemRepository.findAllByOwnerIdOrderById(anyInt()))
                .thenReturn(Collections.emptyList());

        assertEquals(0, itemService.getViewerItems(itemOwner.getId(), 0, 1).size());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findAllByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(0))
                .findAllByOwnerIdOrderById(anyInt());
    }

    @Test
    void getViewerItemsThrowsException() {
        Mockito.when(userService.get(anyInt()))
                .thenReturn(null);
        Mockito.when(itemRepository.findAllByOwnerIdOrderById(anyInt(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());
        Mockito.when(itemRepository.findAllByOwnerIdOrderById(anyInt()))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.getViewerItems(itemOwner.getId(), 5, 0));
        Mockito.verify(itemRepository, Mockito.times(0))
                .findAllByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(0))
                .findAllByOwnerIdOrderById(anyInt());
    }

    @Test
    void searchNoPagination() {
        Mockito.when(userService.get(anyInt()))
                .thenReturn(null);
        Mockito.when(itemRepository.findAllAvailableItemsByWord(anyString(), any(Pageable.class)))
                .thenReturn(List.of(item));
        Mockito.when(itemRepository.findAllAvailableItemsByWord("item"))
                .thenReturn(List.of(item));

        List<Item> result = itemService.search("item", null, null, requestOwner.getId());
        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getId(), Matchers.is(item.getId()));
        assertThat(result.get(0).getName(), Matchers.is(item.getName()));
        assertThat(result.get(0).getDescription(), Matchers.is(item.getDescription()));
    }

    @Test
    void searchWithPagination() {
        Mockito.when(userService.get(anyInt()))
                .thenReturn(null);
        Mockito.when(itemRepository.findAllAvailableItemsByWord(anyString(), any(Pageable.class)))
                .thenReturn(List.of(item));
        Mockito.when(itemRepository.findAllAvailableItemsByWord("item"))
                .thenReturn(List.of(item));

        List<Item> result = itemService.search("item", 0, 1, requestOwner.getId());
        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getId(), Matchers.is(item.getId()));
        assertThat(result.get(0).getName(), Matchers.is(item.getName()));
        assertThat(result.get(0).getDescription(), Matchers.is(item.getDescription()));
    }

    @Test
    void update() {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name("new name")
                .description("new description")
                .available(false)
                .build();
        Mockito.when(userService.get(itemOwner.getId()))
                .thenReturn(itemOwner);
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Item answerItem = itemService.update(item.getId(), itemOwner.getId(), itemDto);
        assertEquals(itemDto.getId(), answerItem.getId());
        assertEquals(itemDto.getName(), answerItem.getName());
        assertEquals(itemDto.getAvailable(), answerItem.getAvailable());
    }

    @Test
    void updateThrowsException() {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name("new name")
                .description("new description")
                .available(false)
                .build();
        Mockito.when(userService.get(itemOwner.getId()))
                .thenReturn(requestOwner);
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(NotFoundException.class, () -> itemService.update(item.getId(), itemOwner.getId(), itemDto));
    }
}