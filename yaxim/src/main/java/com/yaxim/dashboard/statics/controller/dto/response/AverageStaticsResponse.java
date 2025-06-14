package com.yaxim.dashboard.statics.controller.dto.response;

import com.yaxim.dashboard.statics.entity.select.AverageActivity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AverageStaticsResponse {
    private LocalDate report_date;
    private Teams teams;
    private Docs docs;
    private Email email;
    private Git git;

    @Getter
    @AllArgsConstructor
    public static class Docs {
        private Double docx;
        private Double xlsx;
        private Double txt;
        private Double etc;
    }

    @Getter
    @AllArgsConstructor
    public static class Email {
        private Double receive;
        private Double send;
    }

    @Getter
    @AllArgsConstructor
    public static class Git {
        private Double pull_request;
        private Double commit;
        private Double issue;
    }

    @Getter
    @AllArgsConstructor
    public static class Teams {
        private Double post;
    }

    public static AverageStaticsResponse from(AverageActivity activity) {
        return new AverageStaticsResponse(
                activity.getReportDate(),
                new AverageStaticsResponse.Teams(activity.getTeamsPost()),
                new AverageStaticsResponse.Docs(
                        activity.getDocsDocx(),
                        activity.getDocsXlsx(),
                        (double) 0,
                        activity.getDocsEtc()
                ),
                new AverageStaticsResponse.Email(
                        activity.getEmailReceive(),
                        activity.getEmailSend()
                ),
                new AverageStaticsResponse.Git(
                        activity.getGitPullRequest(),
                        activity.getGitCommit(),
                        activity.getGitIssue()
                )
        );
    }
}
