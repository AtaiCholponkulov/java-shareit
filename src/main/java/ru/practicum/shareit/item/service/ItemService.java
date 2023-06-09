package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.mapper.BookingMapper.mapToItemField;
import static ru.practicum.shareit.item.mapper.CommentMapper.map;
import static ru.practicum.shareit.item.mapper.ItemMapper.map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;

    //-----------------------------------------------COMMENT METHODS----------------------------------------------------

    @Transactional
    public Comment add(CommentDto commentDto, int commentatorId, int itemId) {
        LocalDateTime now = LocalDateTime.now();
        commentDto.setCreated(now);
        User commentator = userService.get(commentatorId);
        Item item = this.get(itemId);
        List<Booking> itemBookings = bookingRepository.findAllByItemIdAndStatus(itemId, BookingStatus.APPROVED);
        boolean isOkay = itemBookings.stream().anyMatch(
                booking -> booking.getBooker().getId() == commentatorId
                        && booking.getEndDate().isBefore(now)
                        && booking.getStatus().equals(BookingStatus.APPROVED));
        if (!isOkay) throw new ValidationException("Запрос не прошел проверки");
        Comment comment = map(commentDto, item, commentator);
        return commentRepository.save(comment);
    }

    //------------------------------------------------ITEM METHODS------------------------------------------------------

    @Transactional
    public Item add(ItemDto itemDto, int ownerId) {
        User owner = userService.get(ownerId);
        Item item = map(itemDto, owner);
        return itemRepository.save(item);
    }

    public ItemDtoWithBookingsAndComments get(int itemId, int viewerId) {
        LocalDateTime now = LocalDateTime.now();
        userService.get(viewerId);
        Item dbItem = this.get(itemId);
        List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(itemId);
        ItemDtoWithBookingsAndComments itemDto = map(dbItem, map(comments));
        if (viewerId == dbItem.getOwner().getId())
            setItemDtoLastAndNextBooking(itemDto, now);
        return itemDto;
    }

    public Item get(int itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Такой вещи нет в базе id=" + itemId));
    }

    public List<ItemDtoWithBookingsAndComments> getOwnerItems(int ownerId) {
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findAllByOwnerIdOrderById(ownerId)
                .stream()
                .map(dbItem -> {
                    List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(dbItem.getId());
                    ItemDtoWithBookingsAndComments itemDto = map(dbItem, map(comments));
                    setItemDtoLastAndNextBooking(itemDto, now);
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    public List<Item> search(String word, int viewerId) {
        userService.get(viewerId);
        return word.isBlank() ? new ArrayList<>() : itemRepository.findAllAvailableItemsByWord(word.toLowerCase());
    }

    @Transactional
    public Item update(int itemId, int ownerId, ItemDto itemDto) {
        User owner = userService.get(ownerId);
        Item changedItem = map(itemId, owner, itemDto);
        Item dbItem = this.get(itemId);
        if (!Objects.equals(dbItem.getOwner().getId(), changedItem.getOwner().getId()))
            throw new NotFoundException("Пользователь с id=" + changedItem.getOwner() +
                    " не является владельцем вещи с id=" + changedItem.getId());
        dbItem.update(changedItem);
        return itemRepository.save(dbItem);
    }

    private void setItemDtoLastAndNextBooking(ItemDtoWithBookingsAndComments itemDto, LocalDateTime now) {
        Booking last = null;
        Booking next = null;
        for (Booking current : bookingRepository.findAllByItemIdAndStatus(itemDto.getId(), BookingStatus.APPROVED)) {
            LocalDateTime curBookingStart = current.getStartDate();
            if (curBookingStart.isBefore(now)) {
                if (last == null || last.getStartDate().isBefore(curBookingStart))
                    last = current;
            } else if (curBookingStart.isAfter(now)) {
                if (next == null || next.getStartDate().isAfter(curBookingStart))
                    next = current;
            }
        }
        if (last != null) itemDto.setLastBooking(mapToItemField(last));
        if (next != null) itemDto.setNextBooking(mapToItemField(next));
    }
}
