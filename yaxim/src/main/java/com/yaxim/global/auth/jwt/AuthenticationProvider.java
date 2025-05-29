package com.yaxim.global.auth.jwt;

import com.yaxim.user.entity.user.Users;

public interface AuthenticationProvider {
    String createAccessToken(Users user);
    String createRefreshToken(Users user);
}
