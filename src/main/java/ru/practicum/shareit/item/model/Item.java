package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {

    private Integer id;
    private String name;
    private String description;
    private Boolean available;
    private Integer ownerId;

    public void update(Item item) {
        if (item.getName() != null)
            this.setName(item.getName());
        if (item.getDescription() != null)
            this.setDescription(item.getDescription());
        if (item.getAvailable() != null)
            this.setAvailable(item.getAvailable());
    }
}
