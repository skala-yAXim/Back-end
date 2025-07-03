package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class FileCountExceededException extends CustomException {
    public FileCountExceededException(int fileCount) {
        super(ErrorCode.FILE_COUNT_EXCEEDED, fileCount);
    }
}
