package com.yaxim.project.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class FileDownloadUrlGenerateFailedException extends CustomException {
    public FileDownloadUrlGenerateFailedException() {
        super(ErrorCode.FILE_DOWNLOAD_URL_GENERATE_FAILED);
    }
}
