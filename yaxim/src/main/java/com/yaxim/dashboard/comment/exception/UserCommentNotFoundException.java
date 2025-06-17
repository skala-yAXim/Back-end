package com.yaxim.dashboard.comment.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class UserCommentNotFoundException extends CustomException {
    public UserCommentNotFoundException() {
        super(ErrorCode.USER_COMMENT_NOT_FOUND);
    }
}
