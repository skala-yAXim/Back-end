package com.yaxim.dashboard.statics.entity;

import com.yaxim.team.entity.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class DailyTeamActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate reportDate;
    private Long teamsPost;
    private Long teamsReply;
    private Long emailSend;
    private Long emailReceive;
    private Long docsDocx;
    private Long docsXlsx;
    private Long docsPptx;
    private Long docsEtc;
    private Long gitPullRequest;
    private Long gitCommit;
    private Long gitIssue;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Weekday day;

    public DailyTeamActivity(
            LocalDate reportDate,
            Long teamsPost,
            Long teamsReply,
            Long emailSend,
            Long emailReceive,
            Long docsDocx,
            Long docsXlsx,
            Long docsPptx,
            Long docsEtc,
            Long gitPullRequest,
            Long gitCommit,
            Long gitIssue,
            Team team,
            Weekday day
    ) {
        this.reportDate = reportDate;
        this.teamsPost = teamsPost;
        this.teamsReply = teamsReply;
        this.emailSend = emailSend;
        this.emailReceive = emailReceive;
        this.docsDocx = docsDocx;
        this.docsXlsx = docsXlsx;
        this.docsPptx = docsPptx;
        this.docsEtc = docsEtc;
        this.gitPullRequest = gitPullRequest;
        this.gitCommit = gitCommit;
        this.gitIssue = gitIssue;
        this.team = team;
        this.day = day;
    }
}
