package com.yaxim.global.auth.oauth2;

import com.yaxim.global.auth.CookieService;
import com.yaxim.global.auth.graph.GraphApiService;
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${redirect.uri.success}")
    private String URI;
    private final JwtSecret secrets;
    private final JwtProvider jwtProvider;
    private final CustomOidcUserService oidcUserService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GraphApiService graphApiService;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oAuthToken = (OAuth2AuthenticationToken) authentication;

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                oAuthToken.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        List<String> teams = graphApiService.getUserTeams(accessToken);

        log.info(teams.toString());

        Users user = oidcUserService.getUser();
        log.info(user.getName());

        JwtToken token = jwtProvider.issue(user);

        cookieService.setCookie(
                response,
                token.getAccessToken(),
                token.getRefreshToken()
        );

        // 리다이렉트만 수행
        String redirectUrl = UriComponentsBuilder.fromUriString(URI)
                .build().toUriString();

        response.sendRedirect(redirectUrl);
    }
}
