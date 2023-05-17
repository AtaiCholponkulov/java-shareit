package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private Integer id;
    private String name;
    private String email;

    public void update(User user) {
        if (user.getName() != null)
            this.setName(user.getName());
        if (user.getEmail() != null)
            this.setEmail(user.getEmail());
    }
}
