package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.ItemFieldBookingDto;

import java.util.List;

@Data
@Builder
public class ItemDtoWithBookingsAndComments {

    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private ItemFieldBookingDto lastBooking;
    private ItemFieldBookingDto nextBooking;
    private List<CommentDto> comments;
}
