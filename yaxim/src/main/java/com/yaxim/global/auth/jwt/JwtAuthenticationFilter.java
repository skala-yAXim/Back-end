package com.yaxim.global.auth.jwt;

import com.yaxim.global.auth.CookieService;
import com.yaxim.global.auth.jwt.exception.TokenNotProvidedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CookieService cookieService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${ai.api-key}")
    private String aiApiKey;
    @Value("${ai.header}")
    private String AI_HEADER;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String[] excludePatterns = {
                "/.well-known/**",
                "/webjars/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/favicon.ico",
                "/error",
                "/login",
                "/login/error",
                "/login/oauth2/code/azure",
                "/git/webhook",
                "/auth/reissue",
                "/auth/logout"
        };

        String path = request.getRequestURI();

        return Arrays.stream(excludePatterns)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String path = requestWrapper.getRequestURI();

        if (pathMatcher.match("/api-for-ai/**", path)) {
            String aiToken = request.getHeader(AI_HEADER);

            if (!aiApiKey.equals(aiToken)) {
                throw new TokenNotProvidedException();
            }
        } else {
            String accessToken = cookieService.getAccessTokenFromCookie(request);

            if (accessToken != null) {
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                throw new TokenNotProvidedException();
            }
        }

        filterChain.doFilter(requestWrapper, responseWrapper);
        responseWrapper.copyBodyToResponse();
    }
}
