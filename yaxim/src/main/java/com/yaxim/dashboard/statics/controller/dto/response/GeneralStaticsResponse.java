package com.yaxim.dashboard.statics.controller.dto.response;

import com.yaxim.dashboard.statics.entity.DailyUserActivity;
import com.yaxim.dashboard.statics.entity.DailyTeamActivity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeneralStaticsResponse {
    private LocalDate report_date;
    private Teams teams;
    private Docs docs;
    private Email email;
    private Git git;

    @Getter
    @AllArgsConstructor
    public static class Teams {
        private Long post;
        private Long reply;
    }

    @Getter
    @AllArgsConstructor
    public static class Docs {
        private Long docx;
        private Long xlsx;
        private Long pptx;
        private Long etc;

    }

    @Getter
    @AllArgsConstructor
    public static class Email {
        private Long receive;
        private Long send;

    }

    @Getter
    @AllArgsConstructor
    public static class Git {
        private Long pull_request;
        private Long commit;
        private Long issue;

    }

    public static GeneralStaticsResponse from(DailyUserActivity activity) {
        return new GeneralStaticsResponse(
                activity.getReportDate(),
                new GeneralStaticsResponse.Teams(
                        activity.getTeamsPost(),
                        activity.getTeamsReply()
                ),
                new GeneralStaticsResponse.Docs(
                        activity.getDocsDocx(),
                        activity.getDocsXlsx(),
                        activity.getDocsPptx(),
                        activity.getDocsEtc()
                ),
                new GeneralStaticsResponse.Email(
                        activity.getEmailReceive(),
                        activity.getEmailSend()
                ),
                new GeneralStaticsResponse.Git(
                        activity.getGitPullRequest(),
                        activity.getGitCommit(),
                        activity.getGitIssue()
                )
        );
    }

    public static GeneralStaticsResponse from(DailyTeamActivity activity) {
        return new GeneralStaticsResponse(
                activity.getReportDate(),
                new GeneralStaticsResponse.Teams(
                        activity.getTeamsPost(),
                        activity.getTeamsReply()
                ),
                new GeneralStaticsResponse.Docs(
                        activity.getDocsDocx(),
                        activity.getDocsXlsx(),
                        activity.getDocsPptx(),
                        activity.getDocsEtc()
                ),
                new GeneralStaticsResponse.Email(
                        activity.getEmailReceive(),
                        activity.getEmailSend()
                ),
                new GeneralStaticsResponse.Git(
                        activity.getGitPullRequest(),
                        activity.getGitCommit(),
                        activity.getGitIssue()
                )
        );
    }
}
