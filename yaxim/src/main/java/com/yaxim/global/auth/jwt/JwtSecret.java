package com.yaxim.global.auth.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtSecret {
    @Value("${jwt.secrets}")
    private String secret;
    @Value("${jwt.accessexpire}")
    private long accessTokenValidityInSeconds;
    @Value("${jwt.refreshexpire}")
    private long refreshTokenValidityInSeconds;
}
