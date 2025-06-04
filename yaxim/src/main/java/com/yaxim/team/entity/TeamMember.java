package com.yaxim.team.entity;

import com.yaxim.user.entity.UserRole;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public TeamMember(Team team, String email, UserRole role) {
        this.team = team;
        this.email = email;
        this.role = role;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }
}
