package com.yaxim.global.auth.jwt;

import com.yaxim.user.entity.Users;

public interface AuthenticationProvider {
    String createAccessToken(Users user);
    String createRefreshToken(Users user);
}
