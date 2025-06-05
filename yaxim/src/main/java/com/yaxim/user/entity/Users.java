package com.yaxim.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(unique = true)
    private String email;
    @NotNull
    @Setter
    private String name;
    @NotNull
    @Setter
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    @Setter
    private boolean active;

    public Users(int id, String name, String email) {
        this.id = (long) id;
        this.name = name;
        this.email = email;
    }
}
