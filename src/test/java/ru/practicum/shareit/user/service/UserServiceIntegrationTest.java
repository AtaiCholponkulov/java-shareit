package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.shareit.exception.model.NotFoundException;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceIntegrationTest {

    private final TransactionTemplate tm;
    private final EntityManager em;
    private final UserService userService;

    @Test
    void add() {
        User user = userService.add(new User(null, "user", "user@mail.com"));
        em.detach(user);

        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        User dbUser = query
                .setParameter("id", user.getId())
                .getSingleResult();

        assertThat(dbUser.getId(), equalTo(user.getId()));
        assertThat(dbUser.getName(), equalTo(user.getName()));
        assertThat(dbUser.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void get() {
        User user = new User(null, "user", "user@mail.com");
        tm.execute(status -> {
            em.persist(user);
            return 1;
        });

        User dbUser = userService.get(user.getId());

        assertThat(dbUser.getId(), equalTo(user.getId()));
        assertThat(dbUser.getName(), equalTo(user.getName()));
        assertThat(dbUser.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void getThrowsNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.get(10));
    }

    @Test
    void update() {
        User user = userService.add(new User(null, "user", "user@mail.com"));

        em.detach(user);
        user.setName("new name");
        user.setEmail("newname@mail.com");
        userService.update(user);

        TypedQuery<User> query = em.createQuery("select u from User u where u.id = :id", User.class);
        User dbUser = query
                .setParameter("id", user.getId())
                .getSingleResult();

        assertThat(dbUser.getId(), equalTo(user.getId()));
        assertThat(dbUser.getName(), equalTo(user.getName()));
        assertThat(dbUser.getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void delete() {
        userService.add(new User(null, "user1", "user1@mail.com"));
        User deletedUser = userService.add(new User(null, "user2", "user2@mail.com"));
        userService.add(new User(null, "user3", "user3@mail.com"));
        userService.delete(deletedUser.getId());

        List<User> dbUsers = em.createQuery("select u from User u", User.class).getResultList();

        assertThat(dbUsers, hasSize(2));
        dbUsers.forEach(user -> {
            assertThat(user.getName(), not(is(deletedUser.getName())));
            assertThat(user.getEmail(), not(is(deletedUser.getEmail())));
        });
    }

    @Test
    void getAll() {
        userService.add(new User(null, "user1", "user1@mail.com"));
        userService.add(new User(null, "user2", "user2@mail.com"));
        userService.add(new User(null, "user3", "user3@mail.com"));

        List<User> allUsers = userService.getAll();
        List<User> dbAllUsers = em.createQuery("select u from User u", User.class).getResultList();

        assertThat(allUsers, hasSize(3));
        assertThat(dbAllUsers, hasSize(3));
        for (int i = 0; i < 3; i++) {
            assertThat(allUsers.get(i).getId(), equalTo(dbAllUsers.get(i).getId()));
            assertThat(allUsers.get(i).getName(), equalTo(dbAllUsers.get(i).getName()));
            assertThat(allUsers.get(i).getEmail(), equalTo(dbAllUsers.get(i).getEmail()));
        }
    }
}