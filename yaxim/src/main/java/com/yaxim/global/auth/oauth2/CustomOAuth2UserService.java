package com.yaxim.global.auth.oauth2;

import com.yaxim.user.entity.user.Users;
import com.yaxim.user.entity.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    @Getter
    private Users user;
    @Getter
    private boolean isNewUser;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 유저 정보(attributes) 가져오기
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        // 2. resistrationId 가져오기 (third-party id)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. userNameAttributeName 가져오기
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        // 4. 유저 정보 dto 생성
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

        // 5. 회원가입 및 로그인
        saveOrUpdate(oAuth2UserInfo);

        // 6. OAuth2User로 반환
        return new PrincipalDetails(user, oAuth2UserAttributes, userNameAttributeName);
    }

    private void saveOrUpdate(OAuth2UserInfo oAuth2UserInfo) {
        if(userRepository.existsByEmail(oAuth2UserInfo.getEmail())){
            user = userRepository.findByEmail(oAuth2UserInfo.getEmail())
                    .orElseThrow(UserNotFoundException::new);

            if (!user.isActive()) isNewUser = true;

        } else {
            isNewUser = true;
            user = userRepository.save(oAuth2UserInfo.toEntity());
        }
    }

}
