package com.yaxim.user.entity.user;

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
    private String email;
    @NotNull
    @Setter
    private String name;
    @NotNull
    @Setter
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    @Setter
    private String gitToken;
    @Setter
    private boolean active;
}
