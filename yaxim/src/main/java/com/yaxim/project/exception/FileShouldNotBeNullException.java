package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class FileShouldNotBeNullException extends CustomException {
    public FileShouldNotBeNullException() {
        super(ErrorCode.FILE_SHOULD_NOT_BE_NULL);
    }
}
