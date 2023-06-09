package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findAllByBookerIdOrderByStartDateDesc(Integer bookerId);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "ORDER BY b.startDate DESC")
    List<Booking> findAllByBookingItemOwnerId(Integer ownerId);

    List<Booking> findAllByItemIdAndStatus(Integer itemId, BookingStatus status);
}
