package com.yaxim.global.auth;

import com.yaxim.global.auth.jwt.JwtSecret;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CookieService {
    private final JwtSecret secrets;
    private final String ACCESS_TOKEN = "accessToken";
    private final String REFRESH_TOKEN = "refreshToken";

    public void setCookie(HttpServletResponse response, String accessToken, String refreshToken) {
        // 쿠키 생성 (secure, httpOnly 권장 설정)
        ResponseCookie accessTokenCookie = createCookie(ACCESS_TOKEN, accessToken, secrets.getAccessTokenValidityInSeconds()); // 1시간
        ResponseCookie refreshTokenCookie = createCookie(REFRESH_TOKEN, refreshToken, secrets.getRefreshTokenValidityInSeconds()); // 14일

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    public void deleteCookie(HttpServletResponse response) {
        response.addCookie(clearCookie(ACCESS_TOKEN));
        response.addCookie(clearCookie(REFRESH_TOKEN));
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (REFRESH_TOKEN.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (ACCESS_TOKEN.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private Cookie clearCookie(String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }

    private ResponseCookie createCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
//                .secure(true) // 로컬 개발 시 주석 처리
                .secure(false) // 배포 시 주석 처리
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
    }
}
