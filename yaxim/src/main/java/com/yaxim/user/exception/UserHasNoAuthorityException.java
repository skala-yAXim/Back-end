package com.yaxim.user.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class UserHasNoAuthorityException extends CustomException {
    public UserHasNoAuthorityException() {
        super(ErrorCode.USER_HAS_NO_AUTHORITY);
    }
}
