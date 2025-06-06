package com.yaxim.git.entity;

import com.yaxim.global.util.BaseEntity;
import com.yaxim.user.entity.Users;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class GitInfo extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Users user;

    @Setter
    private String gitId;
    @Setter
    private String gitEmail;
    @Setter
    private String gitUrl;

    public GitInfo(Users user) {
        this.user = user;
    }
}
