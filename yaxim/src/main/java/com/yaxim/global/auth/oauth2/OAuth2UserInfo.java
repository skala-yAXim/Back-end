package com.yaxim.global.auth.oauth2;

import com.yaxim.git.entity.GitInfo;
import com.yaxim.git.exception.GitEmailIsNotProvidedException;
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
        private String gitId;
        private String gitEmail;
        private String gitUrl;
        private String avatarUrl;

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "azure" -> ofAzure(attributes);
            case "github" -> ofGitHub(attributes);
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
                .name(attributes.get("family_name") + (String) attributes.get("given_name"))
                .email(email)
                .build();
    }

    private static OAuth2UserInfo ofGitHub(Map<String, Object> attributes) {
        String name = (String) attributes.get("name");
        String login = (String) attributes.get("login");
        String email = (String) attributes.getOrDefault("email", attributes.get("notification_email"));
        String gitUrl = (String) attributes.get("html_url");
        String avatarUrl = (String) attributes.get("avatar_url");

        if (email == null) {
            throw new GitEmailIsNotProvidedException();
        }

        return OAuth2UserInfo.builder()
                .name(name)
                .gitId(login)
                .gitEmail(email)
                .gitUrl(gitUrl)
                .avatarUrl(avatarUrl)
                .build();
    }

    public Users toEntity() {
        return Users.builder()
                .name(name)
                .email(email)
                .userRole(UserRole.USER)
                .build();
    }

    public GitInfo toGitInfo() {
        return GitInfo.builder()
                .gitId(gitId)
                .gitEmail(gitEmail)
                .gitUrl(gitUrl)
                .avatarUrl(avatarUrl)
                .build();
    }
}
