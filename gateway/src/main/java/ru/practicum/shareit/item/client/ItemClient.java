package ru.practicum.shareit.item.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

import static ru.practicum.shareit.validator.Validator.validatePaginationParams;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }


    public ResponseEntity<Object> addItem(ItemDto itemDto, int ownerId) {
        return post("", ownerId, itemDto);
    }

    public ResponseEntity<Object> getItem(int itemId, int viewerId) {
        return get("/" + itemId, viewerId);
    }

    public ResponseEntity<Object> getViewerItems(int viewerId, Integer from, Integer size) {
        if (validatePaginationParams(from, size)) {
            Map<String, Object> parameters = Map.of(
                    "from", from,
                    "size", size
            );
            return get("?from={from}&size={size}", viewerId, parameters);
        }
        return get("", viewerId);
    }

    public ResponseEntity<Object> search(String text, Integer from, Integer size, int viewerId) {
        Map<String, Object> parameters;
        if (validatePaginationParams(from, size)) {
             parameters = Map.of(
                    "from", from,
                    "size", size,
                    "text", text
            );
            return get("?text={text}&from={from}&size={size}", viewerId, parameters);
        }
        parameters = Map.of("text", text);
        return get("?text={text}", viewerId, parameters);
    }

    public ResponseEntity<Object> updateItem(int itemId, int ownerId, ItemDto itemDto) {
        return patch("/" + itemId, ownerId, itemDto);
    }

    public ResponseEntity<Object> addComment(CommentDto commentDto, int commentatorId, int itemId) {
        return post("/" + itemId + "/comment", commentatorId, commentDto);
    }
}
