package com.yaxim.dashboard.statics.controller.dto.response;

import com.yaxim.dashboard.statics.entity.select.SumActivity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SumStaticResponse {
    private SumStaticResponse.Teams teams;
    private SumStaticResponse.Docs docs;
    private SumStaticResponse.Email email;
    private SumStaticResponse.Git git;

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

    public static SumStaticResponse from(SumActivity activity) {
        return new SumStaticResponse(
                new SumStaticResponse.Teams(
                        activity.getTeamsPost(),
                        activity.getTeamsReply()
                ),
                new SumStaticResponse.Docs(
                        activity.getDocsDocx(),
                        activity.getDocsXlsx(),
                        activity.getDocsPptx(),
                        activity.getDocsEtc()
                ),
                new SumStaticResponse.Email(
                        activity.getEmailReceive(),
                        activity.getEmailSend()
                ),
                new SumStaticResponse.Git(
                        activity.getGitPullRequest(),
                        activity.getGitCommit(),
                        activity.getGitIssue()
                )
        );
    }
}
