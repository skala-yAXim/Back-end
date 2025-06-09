package com.yaxim.graph.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class UserActivityUserDetailApiCallFailedException extends CustomException {
    public UserActivityUserDetailApiCallFailedException() {
        super(ErrorCode.USER_ACTIVITY_USER_DETAIL_API_CALL_FAILED);
    }
}
