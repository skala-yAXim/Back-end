package com.yaxim.team.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TeamMemberNotMappedException extends CustomException {
    public TeamMemberNotMappedException() {
        super(ErrorCode.TEAM_MEMBER_NOT_MAPPED);
    }
}
