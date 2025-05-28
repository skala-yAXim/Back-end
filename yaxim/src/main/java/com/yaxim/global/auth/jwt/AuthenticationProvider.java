package com.yaxim.global.auth.jwt;

import com.yaxim.user.entity.user.UserRole;

public interface AuthenticationProvider {
    String createAccessToken(String userId, UserRole userRole);
    String createRefreshToken(String userId, UserRole userRole);

}
