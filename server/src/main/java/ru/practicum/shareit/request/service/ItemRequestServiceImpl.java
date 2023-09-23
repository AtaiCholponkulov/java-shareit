package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.practicum.shareit.request.mapper.ItemRequestMapper.map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Transactional
    @Override
    public ItemRequest add(int requesterId, ItemRequestDto itemRequestDto) {
        User requester = userRepository.findById(requesterId).orElseThrow(() ->
                new NotFoundException("Такого пользователя нет в базе id=" + requesterId));
        ItemRequest itemRequest = map(itemRequestDto, requester);
        return itemRequestRepository.save(itemRequest);
    }

    @Override
    public ItemRequest get(int requestId) {
        return itemRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("Такого запроса нет в базе id=" + requestId));
    }

    @Override
    public List<ItemRequestDto> getUserRequests(int viewerId) {
        checkUser(viewerId);
        List<ItemRequestDto> requestList = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(viewerId)
                .stream()
                .map(ItemRequestMapper::map)
                .collect(Collectors.toList());
        return addItems(requestList);
    }

    @Override
    public ItemRequestDto get(int viewerId, int requestId) {
        checkUser(viewerId);
        ItemRequestDto request = map(get(requestId));
        List<ItemDto> items = itemRepository.findByRequestId(requestId)
                .stream()
                .map(ItemMapper::map)
                .collect(Collectors.toList());
        request.setItems(items);
        return request;
    }

    @Override
    public List<ItemRequestDto> get(Integer from, Integer size, int viewerId) {
        checkUser(viewerId);
        List<ItemRequest> requestList;
        if (from != null && size != null) {
            Pageable page = PageRequest.of(from, size, Sort.by(Sort.Direction.DESC, "created"));
            requestList = itemRequestRepository.findAll(page).getContent();
        } else {
            requestList = itemRequestRepository.findAll();
        }
        if (requestList.isEmpty()) {
            return new ArrayList<>();
        }
        List<ItemRequestDto> requestDtoList = requestList.stream()
                .filter(itemRequest -> itemRequest.getRequester().getId() != viewerId)
                .map(ItemRequestMapper::map)
                .collect(Collectors.toList());
        return addItems(requestDtoList);
    }

    private void checkUser(int userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Такого пользователя нет в базе id=" + userId);
        }
    }

    private List<ItemRequestDto> addItems(List<ItemRequestDto> requestList) {
        Map<Integer, ItemRequestDto> requestMap = requestList.stream()
                .collect(Collectors.toMap(ItemRequestDto::getId, Function.identity()));
        Map<Integer, List<ItemDto>> itemMap = itemRepository.findByRequestIdIn(requestMap.keySet())
                .stream()
                .map(ItemMapper::map)
                .collect(Collectors.groupingBy(ItemDto::getRequestId));
        return requestMap.values()
                .stream()
                .peek(itemRequestDto -> itemRequestDto.setItems(itemMap.getOrDefault(itemRequestDto.getId(), Collections.emptyList())))
                .collect(Collectors.toList());
    }
}
