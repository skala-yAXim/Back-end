package com.yaxim.graph.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class UserActivityCountsApiCallFailedException extends CustomException {
    public UserActivityCountsApiCallFailedException() {
        super(ErrorCode.USER_ACTIVITY_COUNTS_API_CALL_FAILED);
    }
}
