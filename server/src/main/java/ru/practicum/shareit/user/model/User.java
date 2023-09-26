package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column(unique = true)
    private String email;

    public void update(User user) {
        if (user.getName() != null) {
            this.setName(user.getName());
        }
        if (user.getEmail() != null) {
            this.setEmail(user.getEmail());
        }
    }
}
