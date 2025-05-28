package com.yaxim.global.error.model;

import lombok.Getter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;

import java.util.Locale;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public CustomException(ErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    public CustomException(Throwable e, ErrorCode errorCode, Object... args) {
        super(e.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus();
    }

    public String getCode() {
        return errorCode.name();
    }

    public String getLocalizedMessage(MessageSource messageSource, Locale locale) {
        return messageSource.getMessage(errorCode.name(), args, locale);
    }
}
