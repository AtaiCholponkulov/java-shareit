package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.model.BadRequestException;
import ru.practicum.shareit.exception.model.NotFoundException;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToItemField;
import static ru.practicum.shareit.item.mapper.CommentMapper.map;
import static ru.practicum.shareit.item.mapper.ItemMapper.map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private LocalDateTime moment;

    //------------------------------------------------ITEM METHODS------------------------------------------------------

    @Transactional
    @Override
    public Item add(ItemDto itemDto, int ownerId) {
        User owner = getUser(ownerId);
        Integer requestId = itemDto.getRequestId();
        ItemRequest itemRequest = requestId == null
                ? null
                : itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Такого запроса нет в базе id=" + requestId));
        Item item = map(itemDto, owner, itemRequest);
        return itemRepository.save(item);
    }

    @Override
    public ItemDtoWithBookingsAndComments get(int itemId, int viewerId) {
        moment = LocalDateTime.now();
        checkUser(viewerId);
        Item dbItem = this.get(itemId);
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(itemId);
        ItemDtoWithBookingsAndComments itemDto = map(dbItem, map(comments));
        if (viewerId == dbItem.getOwner().getId()) {
            setItemDtoLastAndNextBooking(itemDto, moment);
        }
        return itemDto;
    }

    @Override
    public Item get(int itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Такой вещи нет в базе id=" + itemId));
    }

    @Override
    public List<ItemDtoWithBookingsAndComments> getViewerItems(int viewerId, Integer from, Integer size) {
        checkUser(viewerId);
        moment = LocalDateTime.now();
        List<Item> itemList;
        if (from != null && size != null) {
            Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.ASC, "id"));
            itemList = itemRepository.findByOwnerIdOrderById(viewerId, page);
        } else {
            itemList = itemRepository.findByOwnerIdOrderById(viewerId);
        }
        if (itemList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, Item> itemMap = itemList.stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));
        Map<Integer, List<Comment>> commentMap = commentRepository.findByItemIdIn(itemMap.keySet())
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));
        return itemMap.values()
                .stream()
                .map(dbItem -> {
                    ItemDtoWithBookingsAndComments itemDto = map(dbItem, map(commentMap.getOrDefault(dbItem.getId(), Collections.emptyList())));
                    setItemDtoLastAndNextBooking(itemDto, moment);
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String word, Integer from, Integer size, int viewerId) {
        checkUser(viewerId);
        if (word.isBlank()) {
            return new ArrayList<>();
        }
        word = word.toLowerCase();
        if (from != null && size != null) {
            Pageable page = PageRequest.of(from, size, Sort.unsorted());
            return itemRepository.findAvailableItemsByWord(word, page);
        } else {
            return itemRepository.findAvailableItemsByWord(word);
        }
    }

    @Transactional
    @Override
    public Item update(int itemId, int ownerId, ItemDto itemDto) {
        User owner = getUser(ownerId);
        Item changedItem = map(itemId, owner, itemDto);
        Item dbItem = this.get(itemId);
        if (!Objects.equals(dbItem.getOwner().getId(), changedItem.getOwner().getId())) {
            throw new NotFoundException("Пользователь с id=" + changedItem.getOwner() +
                    " не является владельцем вещи с id=" + changedItem.getId());
        }
        dbItem.update(changedItem);
        return itemRepository.save(dbItem);
    }

    private void setItemDtoLastAndNextBooking(ItemDtoWithBookingsAndComments itemDto, LocalDateTime now) {
        Pageable page = PageRequest.of(0, 1);
        Slice<Booking> last = bookingRepository.findPrevByItemIdAndStatus(itemDto.getId(), BookingStatus.APPROVED, now, page);
        Slice<Booking> next = bookingRepository.findNextByItemIdAndStatus(itemDto.getId(), BookingStatus.APPROVED, now, page);
        if (last.hasContent()) {
            itemDto.setLastBooking(mapToItemField(last.getContent().get(0)));
        }
        if (next.hasContent()) {
            itemDto.setNextBooking(mapToItemField(next.getContent().get(0)));
        }
    }

    //-----------------------------------------------COMMENT METHODS----------------------------------------------------

    @Transactional
    @Override
    public Comment add(CommentDto commentDto, int commentatorId, int itemId) {
        moment = LocalDateTime.now();
        commentDto.setCreated(moment);
        User commentator = getUser(commentatorId);
        Item item = this.get(itemId);
        List<Booking> itemBookings = bookingRepository.findByItemIdAndStatus(itemId, BookingStatus.APPROVED);
        boolean isOkay = itemBookings.stream().anyMatch(
                booking -> booking.getBooker().getId() == commentatorId
                        && booking.getEndDate().isBefore(moment)
                        && booking.getStatus().equals(BookingStatus.APPROVED));
        if (!isOkay) {
            throw new BadRequestException("Запрос не прошел проверки");
        }
        Comment comment = map(commentDto, item, commentator);
        return commentRepository.save(comment);
    }

    private void checkUser(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Такого пользователя нет в базе id=" + userId);
        }
    }

    private User getUser(int userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Такого пользователя нет в базе id=" + userId));
    }
}
