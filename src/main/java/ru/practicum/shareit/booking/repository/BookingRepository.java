package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findAllByBookerId(Integer bookerId, Pageable pageable);

    List<Booking> findAllByBookerIdOrderByEndDateDesc(Integer bookerId);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findAllByBookingItemOwnerId(Integer ownerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findAllByBookingItemOwnerId(Integer ownerId);

    List<Booking> findAllByItemIdAndStatus(Integer itemId, BookingStatus status);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "WHERE i.id = ?1 " +
            "AND b.status = ?2 " +
            "AND b.startDate > ?3 " +
            "ORDER BY b.startDate ASC")
    Slice<Booking> findNextByItemIdAndStatus(Integer itemId, BookingStatus status, LocalDateTime moment, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "WHERE i.id = ?1 " +
            "AND b.status = ?2 " +
            "AND b.startDate < ?3 " +
            "ORDER BY b.startDate DESC")
    Slice<Booking> findPrevByItemIdAndStatus(Integer itemId, BookingStatus status, LocalDateTime moment, Pageable pageable);
}
