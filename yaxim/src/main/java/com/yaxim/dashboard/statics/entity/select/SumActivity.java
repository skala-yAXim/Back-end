package com.yaxim.dashboard.statics.entity.select;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SumActivity {
    private Long teamsPost;
    private Long teamsReply;
    private Long docsDocx;
    private Long docsXlsx;
    private Long docsPptx;
    private Long docsEtc;
    private Long emailReceive;
    private Long emailSend;
    private Long gitPullRequest;
    private Long gitCommit;
    private Long gitIssue;
}
