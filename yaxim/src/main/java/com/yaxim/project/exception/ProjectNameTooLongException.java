package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ProjectNameTooLongException extends CustomException {
    public ProjectNameTooLongException(int nameLength) {
        super(ErrorCode.PROJECT_NAME_TOO_LONG, nameLength);
    }
}
