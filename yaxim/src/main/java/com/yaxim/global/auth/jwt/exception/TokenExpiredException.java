package com.yaxim.global.auth.jwt.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class TokenExpiredException extends CustomException {
  public TokenExpiredException() {
    super(ErrorCode.TOKEN_EXPIRED_EXCEPTION);
  }
}
