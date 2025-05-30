package com.yaxim.global.error.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR"),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN"),
    TOKEN_EXPIRED_EXCEPTION(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED_EXCEPTION"),
    ILLEGAL_REGISTRATION(HttpStatus.BAD_REQUEST, "ILLEGAL_REGISTRATION"),
    USER_HAS_NO_AUTHORITY(HttpStatus.UNAUTHORIZED, "USER_HAS_NO_AUTHORITY"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"),
    TOKEN_NOT_PROVIDED(HttpStatus.UNAUTHORIZED, "TOKEN_NOT_PROVIDED"),;

    private final HttpStatus status;
    private final String message;
}
