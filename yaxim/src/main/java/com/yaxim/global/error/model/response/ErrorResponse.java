package com.yaxim.global.error.model.response;

import com.yaxim.global.error.model.CustomException;
import lombok.Builder;
import lombok.Getter;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Getter
@Builder
public class ErrorResponse {
    private final String trackingId;
    private final LocalDateTime timestamp;
    private final int status;
    private final String code;
    private final String message;

    public static ErrorResponse of(CustomException e, MessageSource messageSource, Locale locale) {
        return ErrorResponse.builder()
                .trackingId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .status(e.getStatus().value())
                .code(e.getCode())
                .message(e.getLocalizedMessage(messageSource, locale))
                .build();
    }

    public static ErrorResponse of(int status, String code, String message) {
        return ErrorResponse.builder()
                .trackingId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .status(status)
                .code(code)
                .message(message)
                .build();
    }

    public static ErrorResponse of(Exception e) {
        return ErrorResponse.builder()
                .trackingId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .status(500)
                .code(e.getClass().getSimpleName())
                .message(e.getLocalizedMessage())
                .build();
    }
}

