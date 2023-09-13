package ru.practicum.shareit.request.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

import static ru.practicum.shareit.validator.Validator.validatePaginationParams;

@Service
public class ItemRequestClient extends BaseClient {
    private static final String API_PREFIX = "/requests";

    @Autowired
    public ItemRequestClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }


    public ResponseEntity<Object> addRequest(int requesterId, ItemRequestDto itemRequest) {
        return post("", requesterId, itemRequest);
    }

    public ResponseEntity<Object> getUserRequests(int viewerId) {
        return get("", viewerId);
    }

    public ResponseEntity<Object> get(int viewerId, int requestId) {
        return get("/" + requestId, viewerId);
    }

    public ResponseEntity<Object> get(Integer from, Integer size, int viewerId) {
        if (validatePaginationParams(from, size)) {
            Map<String, Object> parameters = Map.of(
                    "from", from,
                    "size", size
            );
            return get("/all?from={from}&size={size}", viewerId, parameters);
        }
        return get("/all", viewerId);
    }
}
