package com.yaxim.global.auth.oauth2;

import com.yaxim.user.entity.user.Users;
import com.yaxim.user.entity.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;
    @Getter
    private Users user;

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        log.info(oidcUser.toString());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oidcUser.getAttributes());

        saveOrUpdate(oAuth2UserInfo);

        return new PrincipalDetails(
                user,
                oidcUser.getAttributes(),
                userNameAttributeName,
                oidcUser.getIdToken(),      // OIDC ID Token
                oidcUser.getUserInfo()      // OIDC UserInfo
        );
    }

    @Transactional
    protected void saveOrUpdate(OAuth2UserInfo oAuth2UserInfo) {
        if(userRepository.existsByEmail(oAuth2UserInfo.getEmail())){
            user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                    .orElseThrow(UserNotFoundException::new);
        } else {
            log.info("new user");
            user = userRepository.save(oAuth2UserInfo.toEntity());
        }
    }
}
