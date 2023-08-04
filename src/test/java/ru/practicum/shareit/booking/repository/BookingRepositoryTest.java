package ru.practicum.shareit.booking.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingRepositoryTest {

    private final EntityManager em;
    private final BookingRepository bookingRepository;
    private User user1;
    private User user2;
    private User user3;
    private Item item1;
    private Item item2;
    private Item item3;
    private Booking booking1;
    private Booking booking2;
    private Booking booking3;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    public void beforeEach() {
        user1 = new User(null, "user1", "user1@com");
        user2 = new User(null, "user2", "user2@com");
        user3 = new User(null, "user3", "user3@com");
        em.persist(user1);
        em.persist(user2);
        em.persist(user3);
        item1 = new Item(null, "item1", "descr", true, user2, null);
        item2 = new Item(null, "item2", "descr", false, user3, null);
        item3 = new Item(null, "item3", "descr", true, user3, null);
        em.persist(item1);
        em.persist(item2);
        em.persist(item3);
        booking1 = new Booking(null, now.minusDays(5), now.minusDays(2), item1, user3, BookingStatus.APPROVED);
        booking2 = new Booking(null, now.plusDays(1), now.plusDays(3), item2, user1, BookingStatus.APPROVED);
        booking3 = new Booking(null, now.minusDays(15), now.minusDays(4), item3, user2, BookingStatus.REJECTED);
        bookingRepository.saveAll(List.of(booking1, booking2, booking3));
        assertNotNull(user1.getId());
        assertNotNull(user2.getId());
        assertNotNull(user3.getId());
        assertNotNull(item1.getId());
        assertNotNull(item2.getId());
        assertNotNull(item3.getId());
        assertNotNull(booking1.getId());
        assertNotNull(booking2.getId());
        assertNotNull(booking3.getId());
    }

    @Test
    void findAllByBookerIdWithPagination() {
        Pageable page = PageRequest.of(0, 1, Sort.by("endDate").descending());

        List<Booking> user1Bookings = bookingRepository.findByBookerId(user1.getId(), page);
        assertEquals(1, user1Bookings.size());
        assertEquals(booking2.getId(), user1Bookings.get(0).getId());
        assertEquals(booking2.getStatus(), user1Bookings.get(0).getStatus());
        assertEquals(booking2.getBooker().getName(), user1Bookings.get(0).getBooker().getName());
        assertEquals(booking2.getStartDate(), user1Bookings.get(0).getStartDate());
        assertEquals(booking2.getEndDate(), user1Bookings.get(0).getEndDate());
    }

    @Test
    void findAllByBookerIdOrderByEndDateDescNoPagination() {
        List<Booking> user2Bookings = bookingRepository.findByBookerIdOrderByEndDateDesc(user2.getId());
        assertEquals(1, user2Bookings.size());
        assertEquals(booking3.getId(), user2Bookings.get(0).getId());
        assertEquals(booking3.getStatus(), user2Bookings.get(0).getStatus());
        assertEquals(booking3.getBooker().getName(), user2Bookings.get(0).getBooker().getName());
        assertEquals(booking3.getStartDate(), user2Bookings.get(0).getStartDate());
        assertEquals(booking3.getEndDate(), user2Bookings.get(0).getEndDate());
    }

    @Test
    void findAllByBookingItemOwnerIdWithPagination() {
        Pageable page = PageRequest.of(0, 5);

        List<Booking> bookingsOfUser3Items = bookingRepository.findByOwnerId(user3.getId(), page);
        assertEquals(2, bookingsOfUser3Items.size());
        assertEquals(booking2.getId(), bookingsOfUser3Items.get(0).getId());
        assertEquals(booking2.getStatus(), bookingsOfUser3Items.get(0).getStatus());
        assertEquals(booking2.getBooker().getName(), bookingsOfUser3Items.get(0).getBooker().getName());
        assertEquals(booking2.getStartDate(), bookingsOfUser3Items.get(0).getStartDate());
        assertEquals(booking2.getEndDate(), bookingsOfUser3Items.get(0).getEndDate());
        assertEquals(booking3.getId(), bookingsOfUser3Items.get(1).getId());
        assertEquals(booking3.getStatus(), bookingsOfUser3Items.get(1).getStatus());
        assertEquals(booking3.getBooker().getName(), bookingsOfUser3Items.get(1).getBooker().getName());
        assertEquals(booking3.getStartDate(), bookingsOfUser3Items.get(1).getStartDate());
        assertEquals(booking3.getEndDate(), bookingsOfUser3Items.get(1).getEndDate());
    }

    @Test
    void findAllByBookingItemOwnerIdNoPagination() {
        List<Booking> bookingsOfUser3Items = bookingRepository.findByOwnerId(user3.getId());
        assertEquals(2, bookingsOfUser3Items.size());
        assertEquals(booking2.getId(), bookingsOfUser3Items.get(0).getId());
        assertEquals(booking2.getStatus(), bookingsOfUser3Items.get(0).getStatus());
        assertEquals(booking2.getBooker().getName(), bookingsOfUser3Items.get(0).getBooker().getName());
        assertEquals(booking2.getStartDate(), bookingsOfUser3Items.get(0).getStartDate());
        assertEquals(booking2.getEndDate(), bookingsOfUser3Items.get(0).getEndDate());
        assertEquals(booking3.getId(), bookingsOfUser3Items.get(1).getId());
        assertEquals(booking3.getStatus(), bookingsOfUser3Items.get(1).getStatus());
        assertEquals(booking3.getBooker().getName(), bookingsOfUser3Items.get(1).getBooker().getName());
        assertEquals(booking3.getStartDate(), bookingsOfUser3Items.get(1).getStartDate());
        assertEquals(booking3.getEndDate(), bookingsOfUser3Items.get(1).getEndDate());
    }

    @Test
    void findAllByItemIdAndStatus() {
        List<Booking> approvedBookingsOfItem1 = bookingRepository
                .findByItemIdAndStatus(item1.getId(), BookingStatus.APPROVED);
        assertEquals(1, approvedBookingsOfItem1.size());
        assertEquals(booking1.getId(), approvedBookingsOfItem1.get(0).getId());
        assertEquals(booking1.getStatus(), approvedBookingsOfItem1.get(0).getStatus());
        assertEquals(booking1.getBooker().getName(), approvedBookingsOfItem1.get(0).getBooker().getName());
        assertEquals(booking1.getStartDate(), approvedBookingsOfItem1.get(0).getStartDate());
        assertEquals(booking1.getEndDate(), approvedBookingsOfItem1.get(0).getEndDate());
    }

    @Test
    void findNextByItemIdAndStatus() {
        Pageable page = PageRequest.of(0, 1);

        Slice<Booking> result = bookingRepository
                .findNextByItemIdAndStatus(item2.getId(), BookingStatus.APPROVED, now, page);
        assertTrue(result.hasContent());
        Booking nextApprovedBookingOfItem2 = result.getContent().get(0);
        assertEquals(booking2.getId(), nextApprovedBookingOfItem2.getId());
        assertEquals(booking2.getStartDate(), nextApprovedBookingOfItem2.getStartDate());
        assertEquals(booking2.getStatus(), nextApprovedBookingOfItem2.getStatus());
        assertEquals(booking2.getItem().getName(), nextApprovedBookingOfItem2.getItem().getName());
        assertEquals(booking2.getBooker().getName(), nextApprovedBookingOfItem2.getBooker().getName());
    }

    @Test
    void findPrevByItemIdAndStatus() {
        Pageable page = PageRequest.of(0, 1);

        Slice<Booking> result = bookingRepository
                .findPrevByItemIdAndStatus(item3.getId(), BookingStatus.REJECTED, now, page);
        assertTrue(result.hasContent());
        Booking nextApprovedBookingOfItem3 = result.getContent().get(0);
        assertEquals(booking3.getId(), nextApprovedBookingOfItem3.getId());
        assertEquals(booking3.getStartDate(), nextApprovedBookingOfItem3.getStartDate());
        assertEquals(booking3.getStatus(), nextApprovedBookingOfItem3.getStatus());
        assertEquals(booking3.getItem().getName(), nextApprovedBookingOfItem3.getItem().getName());
        assertEquals(booking3.getBooker().getName(), nextApprovedBookingOfItem3.getBooker().getName());
    }
}