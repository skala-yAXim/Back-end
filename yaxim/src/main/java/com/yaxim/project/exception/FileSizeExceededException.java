package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class FileSizeExceededException extends CustomException {
    public FileSizeExceededException(String filename, String fileSize) {
        super(ErrorCode.FILE_SIZE_EXCEEDED, filename, fileSize);
    }
}
