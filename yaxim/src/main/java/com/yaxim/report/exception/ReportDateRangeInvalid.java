package com.yaxim.report.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class ReportDateRangeInvalid extends CustomException {
    public ReportDateRangeInvalid() {
        super(ErrorCode.REPORT_DATE_RANGE_INVALID);
    }
}
