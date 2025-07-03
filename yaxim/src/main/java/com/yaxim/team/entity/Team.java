package com.yaxim.team.entity;

import com.yaxim.global.util.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Team extends BaseEntity {
    @Id
    private String id;
    private String name;
    private String description;
    @Setter
    private String installationId;
    @Lob
    @Setter
    private String weeklyTemplate;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TeamMember> members;

    public Team(String id, String displayName, String description) {
        this.id = id;
        this.name = displayName;
        this.description = description;
    }
}
