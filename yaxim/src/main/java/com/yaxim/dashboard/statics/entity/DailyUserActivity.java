package com.yaxim.dashboard.statics.entity;

import com.yaxim.user.entity.Users;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class DailyUserActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Weekday day;

    private LocalDate reportDate;
    private Long teamsPost;
    private Long emailSend;
    private Long emailReceive;
    private Long docsDocx;
    private Long docsXlsx;
    private Long docsTxt;
    private Long docsEtc;
    private Long gitPullRequest;
    private Long gitCommit;
    private Long gitIssue;
}

