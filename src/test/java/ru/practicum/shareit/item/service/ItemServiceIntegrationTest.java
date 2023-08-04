package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBookingsAndComments;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceIntegrationTest {

    private final EntityManager em;
    private final TransactionTemplate tm;
    private final ItemService itemService;
    private User user1;
    private User user2;
    private User user3;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    public void beforeEach() {
        user1 = new User(null, "user1", "user1@mail.com");
        user2 = new User(null, "user2", "user2@mail.com");
        user3 = new User(null, "user3", "user3@mail.com");
        item1 = new Item(null, "item1", "descr1", true, user1, null);
        item2 = new Item(null, "item2", "descr2", false, user2, null);
        item3 = new Item(null, "item3", "descr3", true, user3, null);
        tm.execute(status -> {
            em.persist(user1);
            em.persist(user2);
            em.persist(user3);
            em.persist(item1);
            em.persist(item2);
            em.persist(item3);
            return 1;
        });
    }

    @Test
    void addItem() {
        User user = new User(null, "user", "user@mail.com");
        tm.execute(status -> {
            em.persist(user);
            return 0;
        });
        em.detach(user);
        ItemDto itemDto = ItemDto.builder().name("item").description("descr").available(true).build();
        Item addedItem = itemService.add(itemDto, user.getId());
        em.detach(addedItem);

        TypedQuery<Item> query = em.createQuery("select i from Item i where i.id = :id", Item.class);
        Item dbItem = query
                .setParameter("id", addedItem.getId())
                .getSingleResult();

        assertThat(dbItem.getId(), equalTo(addedItem.getId()));
        assertThat(dbItem.getName(), equalTo(addedItem.getName()));
        assertThat(dbItem.getDescription(), equalTo(addedItem.getDescription()));
        assertThat(dbItem.getAvailable(), equalTo(addedItem.getAvailable()));
        assertThat(dbItem.getOwner().getName(), equalTo(user.getName()));
        assertThat(dbItem.getOwner().getEmail(), equalTo(user.getEmail()));
        assertThat(dbItem.getRequest(), nullValue());
    }

    @Test
    void get() {
        ItemDtoWithBookingsAndComments dbItem = itemService.get(item1.getId(), user1.getId());

        assertThat(dbItem.getId(), is(item1.getId()));
        assertThat(dbItem.getName(), is(item1.getName()));
        assertThat(dbItem.getDescription(), is(item1.getDescription()));
        assertThat(dbItem.getAvailable(), is(item1.getAvailable()));
        assertThat(dbItem.getComments(), hasSize(0));
        assertThat(dbItem.getLastBooking(), nullValue());
        assertThat(dbItem.getNextBooking(), nullValue());
    }

    @Test
    void testGet() {
        Item dbItem = itemService.get(item2.getId());

        assertThat(dbItem.getId(), is(item2.getId()));
        assertThat(dbItem.getName(), is(item2.getName()));
        assertThat(dbItem.getDescription(), is(item2.getDescription()));
        assertThat(dbItem.getAvailable(), is(item2.getAvailable()));
        assertThat(dbItem.getOwner().getName(), is(item2.getOwner().getName()));
        assertThat(dbItem.getRequest(), nullValue());
    }

    @Test
    void getViewerItems() {
        List<ItemDtoWithBookingsAndComments> user1Items = itemService.getViewerItems(user3.getId(), null, null);

        assertThat(user1Items, hasSize(1));
        ItemDtoWithBookingsAndComments dbItem = user1Items.get(0);
        assertThat(dbItem.getId(), is(item3.getId()));
        assertThat(dbItem.getName(), is(item3.getName()));
        assertThat(dbItem.getDescription(), is(item3.getDescription()));
        assertThat(dbItem.getAvailable(), is(item3.getAvailable()));
        assertThat(dbItem.getComments(), hasSize(0));
        assertThat(dbItem.getLastBooking(), nullValue());
        assertThat(dbItem.getNextBooking(), nullValue());
    }

    @Test
    void searchNoPagination() {
        List<Item> result = itemService.search("item3", null, null, user2.getId());

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(item3.getId()));
        assertThat(result.get(0).getName(), is(item3.getName()));
        assertThat(result.get(0).getDescription(), is(item3.getDescription()));
        assertThat(result.get(0).getAvailable(), is(item3.getAvailable()));
    }

    @Test
    void update() {
        item1 = itemService.update(
                item1.getId(),
                user1.getId(),
                ItemDto.builder()
                        .name("new")
                        .description("new")
                        .available(false)
                        .build());
        Item dbItem1 = em
                .createQuery("select i from Item i where i.id = :id", Item.class)
                .setParameter("id", item1.getId())
                .getSingleResult();
        assertThat(dbItem1.getId(), is(item1.getId()));
        assertThat(dbItem1.getAvailable(), is(item1.getAvailable()));
        assertThat(dbItem1.getDescription(), is(item1.getDescription()));
        assertThat(dbItem1.getName(), is(item1.getName()));
        assertThat(dbItem1.getOwner().getName(), is(item1.getOwner().getName()));
        assertThat(dbItem1.getOwner().getEmail(), is(item1.getOwner().getEmail()));
    }

    @Test
    void addComment() {
        Booking booking = new Booking(null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().minusDays(1),
                item1,
                user2,
                BookingStatus.APPROVED);
        tm.execute(status -> {
            em.persist(booking);
            return 0;
        });
        CommentDto commentDto = CommentDto.builder()
                .id(0)
                .text("comment")
                .authorName("author")
                .build();
        int commentatorId = user2.getId();
        Comment comment = itemService.add(commentDto, commentatorId, item1.getId());
        em.detach(comment);
        Comment dbComment = em
                .createQuery("select c from Comment c where c.id = :id", Comment.class)
                .setParameter("id", comment.getId())
                .getSingleResult();

        assertThat(dbComment.getId(), is(comment.getId()));
        assertThat(dbComment.getText(), is(comment.getText()));
        assertThat(dbComment.getCreated(), is(comment.getCreated()));
        assertThat(dbComment.getItem().getName(), is(comment.getItem().getName()));
        assertThat(dbComment.getAuthor().getName(), is(comment.getAuthor().getName()));
    }
}