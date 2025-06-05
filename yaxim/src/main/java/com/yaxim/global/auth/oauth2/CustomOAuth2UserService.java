package com.yaxim.global.auth.oauth2;

import com.yaxim.git.entity.GitInfo;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Getter
@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private Users user;
    private GitInfo gitInfo;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // GitHub로부터 사용자 정보 로딩
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2User.getAttributes());

        gitInfo = oAuth2UserInfo.toGitInfo();

        user = oAuth2UserInfo.toEntity();

        return new PrincipalDetails(
                user,
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }
}
