package com.yaxim.global.auth.oauth2;

import com.yaxim.global.auth.oauth2.exception.IllegalRegistrationException;
import com.yaxim.user.entity.user.UserRole;
import com.yaxim.user.entity.user.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class OAuth2UserInfo {
        private String name;
        private String email;

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) { // registration id별로 userInfo 생성
            case "google" -> ofGoogle(attributes);
            case "micro" -> ofMicro(attributes);
            default -> throw new IllegalRegistrationException();
        };
    }

    private static OAuth2UserInfo ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .build();
    }

    private static OAuth2UserInfo ofMicro(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .build();
    }

    // Todo 권한 설정 로직 수정 필요
    public Users toEntity() {
        return Users.builder()
                .name(name)
                .email(email)
                .userRole(UserRole.USER)
                .build();
    }
}
