package com.yaxim.global.auth.oauth2.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class OidcTokenExpiredException extends CustomException {
    public OidcTokenExpiredException() {
        super(ErrorCode.OIDC_TOKEN_EXPIRED);
    }
}
