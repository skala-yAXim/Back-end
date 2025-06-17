package com.yaxim.dashboard.comment.entity;

import com.yaxim.user.entity.Users;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class UserComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    private String comment;

    @OneToOne(fetch = FetchType.LAZY)
    private Users user;

    public UserComment(Users user, String comment) {
        this.user = user;
        this.comment = comment;
    }
}
