package ru.practicum.shareit.request.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Service
public interface ItemRequestService {
    ItemRequest add(int requesterId, ItemRequestDto itemRequest);

    ItemRequest get(int requestId);

    List<ItemRequestDto> getUserRequests(int ownerId);

    ItemRequestDto get(int viewerId, int requestId);

    List<ItemRequestDto> get(Integer from, Integer size, int viewerId);
}
