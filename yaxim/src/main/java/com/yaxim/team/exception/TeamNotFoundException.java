package com.yaxim.team.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TeamNotFoundException extends CustomException {
    public TeamNotFoundException() {
        super(ErrorCode.TEAM_NOT_FOUND);
    }
}
