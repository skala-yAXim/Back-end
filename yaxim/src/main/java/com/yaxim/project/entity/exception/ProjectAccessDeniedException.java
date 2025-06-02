package com.yaxim.project.entity.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ProjectAccessDeniedException extends CustomException {
    public ProjectAccessDeniedException() {
        super(ErrorCode.PROJECT_ACCESS_DENIED);
    }
}
