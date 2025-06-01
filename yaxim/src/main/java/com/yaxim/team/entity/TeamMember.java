package com.yaxim.team.entity;

import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_email", referencedColumnName = "email")
//    private Users user;

    private String email;

    @Enumerated(EnumType.STRING)
    private UserRole role;

//    public TeamMember(Users user, Team team, UserRole role) {
//        this.user = user;
//        this.team = team;
//        this.role = role;
//    }

    public TeamMember(Team team, String email, UserRole role) {
        this.team = team;
        this.email = email;
        this.role = role;
    }
}
