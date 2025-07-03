package com.yaxim.dashboard.comment.entity;

import com.yaxim.team.entity.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TeamComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String comment;

    @OneToOne(fetch = FetchType.LAZY)
    private Team team;

    public TeamComment(Team team, String comment) {
        this.team = team;
        this.comment = comment;
    }
}
