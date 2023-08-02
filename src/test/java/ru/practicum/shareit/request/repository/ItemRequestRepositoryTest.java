package ru.practicum.shareit.request.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestRepositoryTest {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Test
    void findAllByRequesterIdOrderByCreatedDesc() {
        User requester = new User(null, "requester", "requester@com");
        userRepository.save(requester);
        assertNotNull(requester.getId());
        ItemRequest request = new ItemRequest(null, "need item", requester, LocalDateTime.now());
        itemRequestRepository.save(request);
        assertNotNull(request.getId());
        List<ItemRequest> answer = itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(requester.getId());
        assertEquals(1, answer.size());
        assertEquals(request.getId(), answer.get(0).getId());
        assertEquals(request.getDescription(), answer.get(0).getDescription());
        assertEquals(request.getCreated(), answer.get(0).getCreated());
    }
}