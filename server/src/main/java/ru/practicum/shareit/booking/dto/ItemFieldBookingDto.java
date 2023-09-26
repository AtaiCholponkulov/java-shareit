package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemFieldBookingDto {

    private Integer id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Item item;
    private Integer bookerId;
    private BookingStatus status;
}
