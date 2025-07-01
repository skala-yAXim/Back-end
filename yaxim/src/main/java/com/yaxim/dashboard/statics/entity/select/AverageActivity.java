package com.yaxim.dashboard.statics.entity.select;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AverageActivity {
    private LocalDate reportDate;
    private Double teamsPost;
    private Double teamsReply;
    private Double docsDocx;
    private Double docsXlsx;
    private Double docsPptx;
    private Double docsEtc;
    private Double emailReceive;
    private Double emailSend;
    private Double gitPullRequest;
    private Double gitCommit;
    private Double gitIssue;
}
