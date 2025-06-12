package com.yaxim.statics.entity.select;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class AverageActivity {
    private LocalDate reportDate;
    private Double teamsPost;
    private Double docsDocx;
    private Double docsXlsx;
    private Double docsTxt;
    private Double docsEtc;
    private Double emailReceive;
    private Double emailSend;
    private Double gitPullRequest;
    private Double gitCommit;
    private Double gitIssue;
}
