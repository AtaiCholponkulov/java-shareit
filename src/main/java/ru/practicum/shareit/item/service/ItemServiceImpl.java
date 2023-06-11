package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final CommentRepository commentRepository;

    //-----------------------------------------------COMMENT METHODS----------------------------------------------------

    @Transactional
    @Override
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
    @Override
    public Item add(ItemDto itemDto, int ownerId) {
        User owner = userService.get(ownerId);
        Item item = map(itemDto, owner);
        return itemRepository.save(item);
    }

    @Override
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

    @Override
    public Item get(int itemId) {
        return itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Такой вещи нет в базе id=" + itemId));
    }

    @Override
    public List<ItemDtoWithBookingsAndComments> getViewerItems(int viewerId) {
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findAllByOwnerIdOrderById(viewerId)
                .stream()
                .map(dbItem -> {
                    List<Comment> comments = commentRepository.findAllByItemIdOrderByCreatedDesc(dbItem.getId());
                    ItemDtoWithBookingsAndComments itemDto = map(dbItem, map(comments));
                    setItemDtoLastAndNextBooking(itemDto, now);
                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String word, int viewerId) {
        userService.get(viewerId);
        return word.isBlank() ? new ArrayList<>() : itemRepository.findAllAvailableItemsByWord(word.toLowerCase());
    }

    @Transactional
    @Override
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
        Pageable page = PageRequest.of(0, 1);
        Slice<Booking> last = bookingRepository.findPrevByItemIdAndStatus(itemDto.getId(), BookingStatus.APPROVED, now, page);
        Slice<Booking> next = bookingRepository.findNextByItemIdAndStatus(itemDto.getId(), BookingStatus.APPROVED, now, page);
        if (last.hasContent()) itemDto.setLastBooking(mapToItemField(last.getContent().get(0)));
        if (next.hasContent()) itemDto.setNextBooking(mapToItemField(next.getContent().get(0)));
    }
}
