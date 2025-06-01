package com.yaxim.global.auth.oauth2;

import com.yaxim.global.auth.oauth2.exception.IllegalRegistrationException;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
@Getter
@Builder
@AllArgsConstructor
public class OAuth2UserInfo {
        private String name;
        private String email;

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "azure" -> ofAzure(attributes);
            default -> throw new IllegalRegistrationException();
        };
    }

    private static OAuth2UserInfo ofAzure(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        if (email == null) {
            email = (String) attributes.get("preferred_username");
        }
        if (email == null) {
            email = (String) attributes.get("upn"); // User Principal Name
        }

        return OAuth2UserInfo.builder()
                .name((String) attributes.get("name"))
                .email(email)
                .build();
    }

    // Todo 권한 설정 로직 수정 필요
    public Users toEntity() {
        return Users.builder()
                .name(name)
                .email(email)
                .userRole(UserRole.MEMBER)
                .build();
    }
}
