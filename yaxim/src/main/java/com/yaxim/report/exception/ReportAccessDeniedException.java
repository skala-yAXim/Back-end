package com.yaxim.report.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ReportAccessDeniedException extends CustomException {
    public ReportAccessDeniedException() {
        super(ErrorCode.REPORT_ACCESS_DENIED);
    }}
