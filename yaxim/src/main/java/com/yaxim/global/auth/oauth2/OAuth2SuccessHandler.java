package com.yaxim.global.auth.oauth2;

import com.yaxim.git.entity.GitInfo;
import com.yaxim.git.repository.GitInfoRepository;
import com.yaxim.git.service.GitInfoService;
import com.yaxim.global.auth.CookieService;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.global.auth.jwt.TokenService;
import com.yaxim.global.auth.jwt.JwtProvider;
import com.yaxim.global.auth.jwt.JwtToken;
import com.yaxim.global.auth.jwt.exception.TokenNotProvidedException;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.service.TeamService;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TeamMemberRepository teamMemberRepository;
    @Value("${redirect.uri.success}")
    private String URI;
    private final TokenService tokenService;
    private final TeamService teamService;
    private final UserRepository userRepository;
    private final GitInfoService gitInfoService;
    private final JwtProvider jwtProvider;
    private final CustomOidcUserService oidcUserService;
    private final CustomOAuth2UserService oAuth2UserService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oAuthToken.getAuthorizedClientRegistrationId();

        switch (registrationId) {
            case "azure" -> handleAzureSuccess(oAuthToken, response, authentication);
            case "github" -> handleGithubSuccess(request, response, authentication);
            default -> throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }
    }

    // azure oauth handler
    private void handleAzureSuccess(OAuth2AuthenticationToken oAuthToken, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oAuthToken.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        Users user = oidcUserService.getUser();

        // OIDC Access Token을 레디스에 저장
        tokenService.storeOidcToken(user.getId().toString(), accessToken, Duration.ofHours(1));

        // Teams 정보와 동기화 (내 정보만 저장)
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

    // git oauth handler
    private void handleGithubSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        String token = cookieService.getAccessTokenFromCookie(request);
        if (token == null) {
            throw new TokenNotProvidedException();
        }

        // 로그인 된 사용자 정보 불러오기
        JwtAuthentication auth = jwtProvider.getAuthentication(token);

        // git oauth 정보 불러오기
        GitInfo info = oAuth2UserService.getGitInfo();

        Users user = userRepository.findById(auth.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // git info 업데이트
        gitInfoService.updateGitInfo(user, info);

        // 성공 URL로 리다이렉트
        String redirectUrl = UriComponentsBuilder.fromUriString(URI)
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
