package com.yaxim.global.error;

import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.response.ErrorResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Locale;
import java.util.Optional;

@RestControllerAdvice
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class ControllerAdvisor {

    private final MessageSource messageSource;

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e, Locale locale) {
        log.error("CustomException: {}", e.getMessage(), e);
        ErrorResponse response = ErrorResponse.of(e, messageSource, locale);
        return ResponseEntity.status(e.getStatus()).body(response);
    }

    // @Valid 실패 (DTO 바인딩 실패)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("DTO Validation Exception: {}", e.getMessage());
        return getValidationResponse(e);
    }

    // 일반적인 바인딩 실패 (query param 등)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        log.error("Binding Validation Exception: {}", e.getMessage());
        return getValidationResponse(e);
    }

    // 요청한 url을 찾을 수 없을 때
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoHandlerFoundException e) {
        log.warn("요청 경로를 찾을 수 없음: {}", e.getRequestURL());
        ErrorResponse response = ErrorResponse.of(404, "NOT_FOUND", "요청한 API 경로가 존재하지 않습니다.");
        return ResponseEntity.status(404).body(response);
    }

    // 그밖의 예외처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnknown(Exception e) {
        ErrorResponse response = ErrorResponse.of(e);
        return ResponseEntity.status(500).body(response);
    }

    private ResponseEntity<ErrorResponse> getValidationResponse(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0); // 첫 번째 오류만
        String code = Optional.ofNullable(fieldError.getCode())
                .map(String::toUpperCase)
                .orElse("INVALID");

        String message = fieldError.getDefaultMessage();

        ErrorResponse response = ErrorResponse.of(
                400,
                fieldError.getField().toUpperCase() + "_" + code,
                message);
        return ResponseEntity.badRequest().body(response);
    }
}

