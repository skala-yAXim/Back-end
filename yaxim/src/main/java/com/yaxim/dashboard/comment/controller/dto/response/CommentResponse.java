package com.yaxim.dashboard.comment.controller.dto.response;

import com.yaxim.dashboard.comment.entity.TeamComment;
import com.yaxim.dashboard.comment.entity.UserComment;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentResponse {
    private String comment;

    public static CommentResponse from(UserComment comment) {
        return new CommentResponse(comment.getComment());
    }

    public static CommentResponse from(TeamComment comment) {
        return new CommentResponse(comment.getComment());
    }
}
