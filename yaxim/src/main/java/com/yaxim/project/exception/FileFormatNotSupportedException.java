package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class FileFormatNotSupportedException extends CustomException {
    public FileFormatNotSupportedException() {
        super(ErrorCode.FILE_FORMAT_NOT_SUPPORTED);
    }
}
