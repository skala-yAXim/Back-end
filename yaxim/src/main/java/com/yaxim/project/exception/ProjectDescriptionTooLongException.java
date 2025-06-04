package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ProjectDescriptionTooLongException extends CustomException {
    public ProjectDescriptionTooLongException(int descriptionLength) {
        super(ErrorCode.PROJECT_DESCRIPTION_TOO_LONG, descriptionLength);
    }
}
