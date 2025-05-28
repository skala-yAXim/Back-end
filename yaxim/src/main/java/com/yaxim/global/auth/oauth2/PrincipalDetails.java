package com.yaxim.global.auth.oauth2;

import com.yaxim.user.entity.user.Users;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@AllArgsConstructor
public class PrincipalDetails implements OAuth2User, UserDetails {
    Users user;
    Map<String, Object> attributes;
    String attributeKey;

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getUserRole().toString()));
    }

    @Override
    public String getName() {
        return "";
    }
}
