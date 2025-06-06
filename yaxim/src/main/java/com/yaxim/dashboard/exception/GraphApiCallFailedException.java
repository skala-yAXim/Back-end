package com.yaxim.dashboard.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class GraphApiCallFailedException extends CustomException {
    public GraphApiCallFailedException() {
        super(ErrorCode.GRAPH_API_CALL_FAILED);
    }
}