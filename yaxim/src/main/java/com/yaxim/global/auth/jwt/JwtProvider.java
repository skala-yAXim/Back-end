package com.yaxim.global.auth.jwt;

import com.yaxim.global.auth.jwt.exception.InvalidTokenException;
import com.yaxim.global.auth.jwt.exception.TokenExpiredException;
import com.yaxim.user.entity.user.UserRole;
import com.yaxim.user.entity.user.Users;
import com.yaxim.user.entity.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtProvider implements AuthenticationProvider {

    private final JwtSecret secrets;
    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secrets.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String BEARER = "bearer ";
    public static final String AUTHORIZATION = "Authorization";

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public JwtToken issue(Users user){
        return JwtToken.builder()
                .accessToken(createAccessToken(user.getId().toString(), user.getUserRole()))
                .refreshToken(createRefreshToken(user.getId().toString(), user.getUserRole()))
                .build();
    }

    public JwtAuthentication getAuthentication(String accessToken) {
        Jws<Claims> claimsJws = validateAccessToken(accessToken);

        Claims body = claimsJws.getBody();
        Long userId = Long.parseLong((String) body.get("userId"));
        UserRole userRole = UserRole.of((String) body.get("userRole"));

        return new JwtAuthentication(userId, userRole);
    }

    @Override
    public String createAccessToken(String userId, UserRole userRole) {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        return Jwts.builder()
                .setSubject(ACCESS_TOKEN_SUBJECT)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + secrets.getAccessTokenValidityInSeconds() * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String createRefreshToken(String userId, UserRole userRole) {
        String jti = UUID.randomUUID().toString();

        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        tokenService.storeRefreshTokenJti(userId, jti);

        return Jwts
                .builder()
                .setSubject(REFRESH_TOKEN_SUBJECT)
                .setClaims(claims)
                .setId(jti)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + secrets.getRefreshTokenValidityInSeconds() * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String refreshRefreshToken(String userId, UserRole userRole) {
        String jti = UUID.randomUUID().toString();
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        claims.put("userRole", userRole);

        tokenService.resetRefreshTokenJti(userId, jti);

        return Jwts
                .builder()
                .setSubject(REFRESH_TOKEN_SUBJECT)
                .setClaims(claims)
                .setId(jti)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + secrets.getRefreshTokenValidityInSeconds() * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> validateAccessToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    public Jws<Claims> validateRefreshToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException e) {
            throw new InvalidTokenException();
        }
    }

    public JwtToken reissue(String refreshToken) {
        String userId = getUserIdFromToken(refreshToken);

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        return JwtToken.builder()
                .accessToken(createAccessToken(user.getId().toString(), user.getUserRole()))
                .refreshToken(refreshRefreshToken(user.getId().toString(), user.getUserRole()))
                .build();
    }

    public String getUserIdFromToken(String token) {
        Jws<Claims> claims = validateRefreshToken(token);

        String jti = claims.getBody().getId();
        String userId = claims.getBody().get("userId", String.class);

        if (!tokenService.isRefreshTokenValid(userId, jti)){
            throw new InvalidTokenException();
        }

        return userId;
    }

    public String getAccessTokenFromHeader(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("access-token")) {
                    return cookie.getValue();
                }
            }
        }

        String header = request.getHeader(AUTHORIZATION);
        if (header != null) {
            if (!header.toLowerCase().startsWith(BEARER)) {
                throw new RuntimeException();
            }
            return header.substring(7);
        }

        return null;
    }
}
