package com.yaxim.dashboard.comment.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TeamCommentNotFoundException extends CustomException {
    public TeamCommentNotFoundException() {
        super(ErrorCode.TEAM_COMMENT_NOT_FOUND);
    }
}
