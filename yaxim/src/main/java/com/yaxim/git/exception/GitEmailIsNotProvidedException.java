package com.yaxim.git.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class GitEmailIsNotProvidedException extends CustomException {
    public GitEmailIsNotProvidedException() {
        super(ErrorCode.GIT_EMAIL_IS_NOT_PROVIDED);
    }
}
