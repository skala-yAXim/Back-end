package com.yaxim.statics.entity.select;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class TeamActivity {
    private LocalDate reportDate;
    private Long teamsPost;
    private Long docsDocx;
    private Long docsXlsx;
    private Long docsTxt;
    private Long docsEtc;
    private Long emailReceive;
    private Long emailSend;
    private Long gitPullRequest;
    private Long gitCommit;
    private Long gitIssue;
}
