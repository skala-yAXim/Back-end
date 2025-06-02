package com.yaxim.project.entity.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ProjectNotFoundException extends CustomException {
    public ProjectNotFoundException() {
        super(ErrorCode.PROJECT_NOT_FOUND);
    }
}
