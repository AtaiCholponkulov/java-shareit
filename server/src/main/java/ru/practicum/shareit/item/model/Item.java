package ru.practicum.shareit.item.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String description;

    @Column(name = "is_available")
    private Boolean available;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id")
    private ItemRequest request;

    public void update(Item item) {
        if (item.getName() != null) {
            this.setName(item.getName());
        }
        if (item.getDescription() != null) {
            this.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            this.setAvailable(item.getAvailable());
        }
    }
}
