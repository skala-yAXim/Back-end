package com.yaxim.graph.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TeamActivityDetailApiCallFailed extends CustomException {
    public TeamActivityDetailApiCallFailed() {
        super(ErrorCode.TEAM_ACTIVITY_DETAIL_API_CALL_FAILED);
    }
}
