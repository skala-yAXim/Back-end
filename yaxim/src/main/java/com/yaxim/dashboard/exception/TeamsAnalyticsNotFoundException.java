package com.yaxim.dashboard.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TeamsAnalyticsNotFoundException extends CustomException {
    public TeamsAnalyticsNotFoundException() {
        super(ErrorCode.TEAMS_ANALYTICS_NOT_FOUND);
    }
}
