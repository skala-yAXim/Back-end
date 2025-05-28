package com.yaxim.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaxim.global.error.model.CustomException;
import com.yaxim.global.error.model.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorHandleFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            handleException(response, e, e.getStatus().value(), request.getLocale());
        } catch (Exception e) {
            handleException(response, e);
        }
    }

    private void handleException(HttpServletResponse response,
                                 CustomException e,
                                 int statusCode,
                                 Locale locale) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.of(e, messageSource, locale);
        log.error("Exception in filter: trackingId={}, code={}, message={}",
                errorResponse.getTrackingId(), errorResponse.getCode(), errorResponse.getMessage(), e);

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    private void handleException(HttpServletResponse response,
                                 Exception e) throws IOException {
        ErrorResponse errorResponse = ErrorResponse.of(e);
        log.error("Exception in filter: trackingId={}, code={}, message={}",
                errorResponse.getTrackingId(), errorResponse.getCode(), errorResponse.getMessage(), e);

        response.setStatus(500);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
