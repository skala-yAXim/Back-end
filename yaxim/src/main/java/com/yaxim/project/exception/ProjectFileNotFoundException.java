package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ProjectFileNotFoundException extends CustomException {
    public ProjectFileNotFoundException() {
        super(ErrorCode.PROJECT_FILE_NOT_FOUND, "프로젝트 파일을 찾을 수 없습니다.");
    }

    public ProjectFileNotFoundException(Long fileId) {
        super(ErrorCode.PROJECT_FILE_NOT_FOUND, "ID " + fileId + "에 해당하는 프로젝트 파일을 찾을 수 없습니다.");
    }
}
