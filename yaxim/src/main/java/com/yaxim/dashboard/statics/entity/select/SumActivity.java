package com.yaxim.dashboard.statics.entity.select;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SumActivity {
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
