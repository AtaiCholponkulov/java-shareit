package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByBookerIdOrderByEndDateDesc(Integer bookerId);//all

    List<Booking> findByBookerIdAndEndDateBeforeOrderByEndDateDesc(Integer bookerId, LocalDateTime moment);//past

    List<Booking> findByBookerIdAndStartDateBeforeAndEndDateAfterOrderByEndDateDesc(Integer bookerId, LocalDateTime moment, LocalDateTime anotherMoment);//current

    List<Booking> findByBookerIdAndStartDateAfterOrderByEndDateDesc(Integer bookerId, LocalDateTime moment);//future

    List<Booking> findByBookerIdAndStatusOrderByEndDateDesc(Integer bookerId, BookingStatus status);//bookingStatus

    List<Booking> findByBookerId(Integer bookerId, Pageable pageable);//all

    List<Booking> findByBookerIdAndEndDateBefore(Integer bookerId, LocalDateTime moment, Pageable pageable);//past

    List<Booking> findByBookerIdAndStartDateBeforeAndEndDateAfter(Integer bookerId, LocalDateTime moment, LocalDateTime anotherMoment, Pageable pageable);//current

    List<Booking> findByBookerIdAndStartDateAfter(Integer bookerId, LocalDateTime moment, Pageable pageable);//future

    List<Booking> findByBookerIdAndStatus(Integer bookerId, BookingStatus status, Pageable pageable);//bookingStatus

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerId(Integer ownerId);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.endDate < ?2 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdPast(Integer ownerId, LocalDateTime moment);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.endDate > ?2 " +
            "AND b.startDate < ?3 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdCurrent(Integer ownerId, LocalDateTime moment, LocalDateTime anotherMoment);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.startDate > ?2 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdFuture(Integer ownerId, LocalDateTime moment);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.status LIKE ?2 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdAndStatus(Integer ownerId, BookingStatus bookingStatus);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerId(Integer ownerId, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.endDate < ?2 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdPast(Integer ownerId, LocalDateTime moment, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.endDate > ?2 " +
            "AND b.startDate < ?3 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdCurrent(Integer ownerId, LocalDateTime moment, LocalDateTime anotherMoment, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.startDate > ?2 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdFuture(Integer ownerId, LocalDateTime moment, Pageable pageable);

    @Query("SELECT b " +
            "FROM Booking AS b " +
            "JOIN b.item AS i " +
            "JOIN i.owner AS o " +
            "WHERE o.id = ?1 " +
            "AND b.status LIKE ?2 " +
            "ORDER BY b.endDate DESC")
    List<Booking> findByOwnerIdAndStatus(Integer ownerId, BookingStatus bookingStatus, Pageable pageable);

    List<Booking> findByItemIdAndStatus(Integer itemId, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStatusAndEndDateAfterOrderByStartDateAsc(Integer itemId, BookingStatus status, LocalDateTime moment);

    Optional<Booking> findFirstByItemIdAndStatusAndStartDateBeforeOrderByEndDateDesc(Integer itemId, BookingStatus status, LocalDateTime moment);
}
