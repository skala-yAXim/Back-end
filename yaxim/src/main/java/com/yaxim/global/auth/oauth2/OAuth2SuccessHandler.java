package com.yaxim.global.auth.oauth2;

import com.yaxim.global.auth.CookieService;
import com.yaxim.global.auth.jwt.TokenService;
import com.yaxim.global.auth.jwt.JwtProvider;
import com.yaxim.global.auth.jwt.JwtToken;
import com.yaxim.team.service.TeamService;
import com.yaxim.user.entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final TeamService teamService;
    @Value("${redirect.uri.success}")
    private String URI;
    private final JwtProvider jwtProvider;
    private final CustomOidcUserService oidcUserService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CookieService cookieService;

    /*
    OAuth 로그인 성공 시 OIDC Access Token을 레디스에 저장, 해당 토큰으로 팀 정보 동기화 (변경 사항 있으면 업데이트 됨)
    최종적으로 JWT Token 발급함
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oAuthToken.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        Users user = oidcUserService.getUser();

        // OIDC Access Token을 레디스에 저장
        tokenService.storeOidcToken(user.getId().toString(), accessToken, Duration.ofHours(1));

        // Teams 정보와 동기화
        teamService.loadTeam(user.getId());

        // JWT Token 발급
        JwtToken token = jwtProvider.issue(user);

        // 쿠키 발급
        cookieService.setCookie(
                response,
                token.getAccessToken(),
                token.getRefreshToken()
        );

        // 성공 URL로 리다이렉트
        String redirectUrl = UriComponentsBuilder.fromUriString(URI)
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
