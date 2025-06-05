package com.yaxim.git.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class GitInfoNotFoundException extends CustomException {
    public GitInfoNotFoundException() {
        super(ErrorCode.GIT_INFO_NOT_FOUND);
    }
}
