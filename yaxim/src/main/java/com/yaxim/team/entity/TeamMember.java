package com.yaxim.team.entity;

import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    public TeamMember(Team team, Users user, UserRole role) {
        this.team = team;
        this.user = user;
        this.role = role;
    }

    public TeamMember(Team team, Users user) {
        this.team = team;
        this.user = user;
    }

    public void updateRole(UserRole role) {
        this.role = role;
    }
}
