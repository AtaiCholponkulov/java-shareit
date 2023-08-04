package ru.practicum.shareit.item.service;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceUnitTest {

    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @InjectMocks
    private ItemServiceImpl itemService;
    private User requestOwner;
    private ItemRequest itemRequest;
    private User itemOwner;
    private Item item;
    private LocalDateTime now;

    @BeforeEach
    void beforeEach() {
        now = LocalDateTime.now();
        requestOwner = new User(0, "requester", "requester@com");
        itemRequest = new ItemRequest(0, "itemRequest", requestOwner, now.minusMonths(1));
        itemOwner = new User(1, "owner", "owner@com");
        item = new Item(0, "item", "itemDescription", true, itemOwner, itemRequest);
    }

    @Test
    void addItemWithoutRequest() {
        ItemDto itemDto = ItemDto.builder()
                .id(0)
                .name(item.getName())
                .description(item.getDescription())
                .available(true)
                .build();
        Mockito.when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.save(any(Item.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Item answerItem = itemService.add(itemDto, itemOwner.getId());
        Mockito.verifyNoInteractions(itemRequestRepository);
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
        Mockito.when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.of(itemRequest));
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
    void addItemRequestNotFoundException() {
        ItemDto itemDto = ItemDto.builder()
                .id(0)
                .name(item.getName())
                .description(item.getDescription())
                .available(true)
                .requestId(itemRequest.getId())
                .build();
        Mockito.when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRequestRepository.findById(itemRequest.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.add(itemDto, itemOwner.getId()));
    }

    @Test
    void addItemThrowsException() {
        ItemDto itemDto = ItemDto.builder()
                .id(0)
                .name(item.getName())
                .description(item.getDescription())
                .available(true)
                .requestId(itemRequest.getId())
                .build();
        Mockito.when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.add(itemDto, itemOwner.getId()));
    }

    @Test
    void getItemWithBookingsAndComments() {
        Mockito.when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(commentRepository.findByItemIdOrderByCreatedDesc(item.getId()))
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
        Mockito.when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findByOwnerIdOrderById(anyInt()))
                .thenReturn(Collections.emptyList());

        assertEquals(0, itemService.getViewerItems(itemOwner.getId(), null, null).size());
        Mockito.verify(itemRepository, Mockito.times(0))
                .findByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findByOwnerIdOrderById(anyInt());
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
        Mockito.when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findByOwnerIdOrderById(anyInt()))
                .thenReturn(List.of(item));
        Mockito.when(commentRepository.findByItemIdIn(Mockito.anySet()))
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
                any(Pageable.class))).thenReturn(new SliceImpl<>(List.of(booking)));

        List<ItemDtoWithBookingsAndComments> result = itemService.getViewerItems(itemOwner.getId(), null, null);
        Mockito.verify(itemRepository, Mockito.times(0))
                .findByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(1))
                .findByOwnerIdOrderById(anyInt());
        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getId(), Matchers.is(item.getId()));
        assertThat(result.get(0).getName(), Matchers.is(item.getName()));
        assertThat(result.get(0).getAvailable(), Matchers.is(item.getAvailable()));
        assertThat(result.get(0).getNextBooking(), Matchers.notNullValue());
        assertThat(result.get(0).getNextBooking().getStart(), Matchers.is(booking.getStartDate()));
        assertThat(result.get(0).getNextBooking().getEnd(), Matchers.is(booking.getEndDate()));
        assertThat(result.get(0).getLastBooking(), Matchers.notNullValue());
        assertThat(result.get(0).getLastBooking().getStart(), Matchers.is(booking.getStartDate()));
        assertThat(result.get(0).getLastBooking().getEnd(), Matchers.is(booking.getEndDate()));
        assertThat(result.get(0).getComments(), Matchers.hasSize(1));
        assertThat(result.get(0).getComments().get(0).getText(), Matchers.is(comment.getText()));
    }

    @Test
    void getViewerItemsWithPagination() {
        Mockito.when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(itemOwner));
        Mockito.when(itemRepository.findByOwnerIdOrderById(anyInt(), any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        assertEquals(0, itemService.getViewerItems(itemOwner.getId(), 0, 1).size());
        Mockito.verify(itemRepository, Mockito.times(1))
                .findByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(0))
                .findByOwnerIdOrderById(anyInt());
    }

    @Test
    void getViewerItemsThrowsException() {
        Mockito.when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(itemOwner));

        assertThrows(ValidationException.class, () -> itemService.getViewerItems(itemOwner.getId(), 5, 0));
        Mockito.verify(itemRepository, Mockito.times(0))
                .findByOwnerIdOrderById(anyInt(), any(Pageable.class));
        Mockito.verify(itemRepository, Mockito.times(0))
                .findByOwnerIdOrderById(anyInt());
    }

    @Test
    void searchNoPagination() {
        Mockito.when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(requestOwner));
        Mockito.when(itemRepository.findAvailableByWord("item"))
                .thenReturn(List.of(item));

        List<Item> result = itemService.search("item", null, null, requestOwner.getId());
        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getId(), Matchers.is(item.getId()));
        assertThat(result.get(0).getName(), Matchers.is(item.getName()));
        assertThat(result.get(0).getDescription(), Matchers.is(item.getDescription()));
    }

    @Test
    void searchWithPagination() {
        Mockito.when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(requestOwner));
        Mockito.when(itemRepository.findAvailableByWord(anyString(), any(Pageable.class)))
                .thenReturn(List.of(item));

        List<Item> result = itemService.search("item", 0, 1, requestOwner.getId());
        assertThat(result, Matchers.hasSize(1));
        assertThat(result.get(0).getId(), Matchers.is(item.getId()));
        assertThat(result.get(0).getName(), Matchers.is(item.getName()));
        assertThat(result.get(0).getDescription(), Matchers.is(item.getDescription()));
    }

    @Test
    void searchWithPaginationEmptySearchText() {
        Mockito.when(userRepository.findById(anyInt()))
                .thenReturn(Optional.of(requestOwner));

        List<Item> result = itemService.search("", 0, 1, requestOwner.getId());
        assertThat(result, Matchers.hasSize(0));
    }

    @Test
    void update() {
        ItemDto itemDto = ItemDto.builder()
                .id(item.getId())
                .name("new name")
                .description("new description")
                .available(false)
                .build();
        Mockito.when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(itemOwner));
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
        Mockito.when(userRepository.findById(itemOwner.getId()))
                .thenReturn(Optional.of(requestOwner));
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.update(item.getId(), itemOwner.getId(), itemDto));
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

        Mockito.when(userRepository.findById(commentatorId))
                .thenReturn(Optional.of(requestOwner));
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findByItemIdAndStatus(item.getId(), BookingStatus.APPROVED))
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

        Mockito.when(userRepository.findById(commentatorId))
                .thenReturn(Optional.of(requestOwner));
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findByItemIdAndStatus(item.getId(), BookingStatus.APPROVED))
                .thenReturn(List.of(booking));

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

        Mockito.when(userRepository.findById(commentatorId))
                .thenReturn(Optional.of(requestOwner));
        Mockito.when(itemRepository.findById(item.getId()))
                .thenReturn(Optional.of(item));
        Mockito.when(bookingRepository.findByItemIdAndStatus(item.getId(), BookingStatus.APPROVED))
                .thenReturn(Collections.emptyList());

        assertThrows(ValidationException.class, () -> itemService.add(commentDto, commentatorId, item.getId()));
    }
}