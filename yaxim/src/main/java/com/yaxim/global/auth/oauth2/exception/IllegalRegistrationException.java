package com.yaxim.global.auth.oauth2.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class IllegalRegistrationException extends CustomException {
    public IllegalRegistrationException() {
        super(ErrorCode.ILLEGAL_REGISTRATION);
    }
}
