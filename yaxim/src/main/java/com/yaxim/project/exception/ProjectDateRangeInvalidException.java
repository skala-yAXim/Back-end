package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ProjectDateRangeInvalidException extends CustomException {
    public ProjectDateRangeInvalidException() {
        super(ErrorCode.PROJECT_DATE_RANGE_INVALID);
    }
}
