package com.yaxim.graph.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TeamActivityCountsApiCallFailed extends CustomException {
    public TeamActivityCountsApiCallFailed() {
        super(ErrorCode.TEAM_ACTIVITY_COUNTS_API_CALL_FAILED);
    }
}
