package com.yaxim.global.error.model;

public class UnknownException extends CustomException {
    public UnknownException(Throwable e) {
        super(e, ErrorCode.UNKNOWN_ERROR);
    }
}
