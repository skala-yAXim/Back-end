package com.yaxim.global.auth.jwt.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TokenNotProvidedException extends CustomException {
    public TokenNotProvidedException() {
        super(ErrorCode.TOKEN_NOT_PROVIDED);
    }
}
