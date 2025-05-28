package com.yaxim.global.auth.oauth2;

import com.yaxim.global.auth.jwt.JwtProvider;
import com.yaxim.global.auth.jwt.JwtSecret;
import com.yaxim.global.auth.jwt.JwtToken;
import com.yaxim.user.entity.user.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseCookie;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${redirect.uri}")
    private String URI;
    private final JwtSecret secrets;
    private final JwtProvider jwtProvider;
    private final CustomOAuth2UserService oAuth2UserService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Users user = oAuth2UserService.getUser();
        boolean isNewUser = oAuth2UserService.isNewUser();

        // accessToken, refreshToken 발급
        JwtToken token = jwtProvider.issue(user);

        // 쿠키 생성 (secure, httpOnly 권장 설정)
        ResponseCookie accessTokenCookie = createCookie("accessToken", token.getAccessToken(), secrets.getAccessTokenValidityInSeconds()); // 1시간
        ResponseCookie refreshTokenCookie = createCookie("refreshToken", token.getRefreshToken(), secrets.getRefreshTokenValidityInSeconds()); // 14일

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        // 리다이렉트만 수행
        String redirectUrl = UriComponentsBuilder.fromUriString(URI)
                .queryParam("isNewUser", isNewUser)
                .queryParam("userRole", user.getUserRole())
                .build().toUriString();

        response.sendRedirect(redirectUrl);
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
