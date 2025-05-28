package com.yaxim.global.auth.jwt.exception;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.ErrorCode;

public class InvalidTokenException extends CustomException {
  public InvalidTokenException() {
    super(ErrorCode.INVALID_TOKEN);
  }
}
