package com.yaxim.global.auth.jwt;

import com.yaxim.global.auth.jwt.exception.InvalidTokenException;
import com.yaxim.global.auth.jwt.exception.TokenExpiredException;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
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

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public JwtToken issue(Users user){
        return JwtToken.builder()
                .accessToken(createAccessToken(user))
                .refreshToken(createRefreshToken(user))
                .build();
    }

    public JwtAuthentication getAuthentication(String accessToken) {
        Jws<Claims> claims = validateToken(accessToken, ACCESS_TOKEN_SUBJECT);

        Claims body = claims.getBody();
        Long userId = Long.parseLong(body.get("userId").toString());
        UserRole userRole = UserRole.of(body.get("userRole").toString());
        String email = body.get("email").toString();

        return new JwtAuthentication(userId, email, userRole);
    }

    private Claims buildClaims(Users user) {
        Claims claims = Jwts.claims();
        claims.put("userId", user.getId());
        claims.put("name", user.getName());
        claims.put("email", user.getEmail());
        claims.put("userRole", user.getUserRole());

        return claims;
    }

    @Override
    public String createAccessToken(Users user) {
        String jti = UUID.randomUUID().toString();
        Claims claims = buildClaims(user);

        tokenService.storeAccessTokenJti(user.getId().toString(), jti, Duration.ofSeconds(secrets.getAccessTokenValidityInSeconds()));

        return Jwts.builder()
                .setSubject(ACCESS_TOKEN_SUBJECT)
                .setClaims(claims)
                .setId(jti)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + secrets.getAccessTokenValidityInSeconds() * 1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public String createRefreshToken(Users user) {
        String jti = UUID.randomUUID().toString();
        Claims claims = buildClaims(user);

        tokenService.storeRefreshTokenJti(user.getId().toString(), jti, Duration.ofSeconds(secrets.getRefreshTokenValidityInSeconds()));

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

    public String refreshRefreshToken(Users user) {
        String jti = UUID.randomUUID().toString();
        Claims claims = buildClaims(user);

        tokenService.resetRefreshTokenJti(user.getId().toString(), jti, Duration.ofSeconds(secrets.getRefreshTokenValidityInSeconds()));

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

    public Jws<Claims> validateToken(String token, String type) {
        try {
            if (token == null || token.isEmpty()) {
                throw new InvalidTokenException();
            }

            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Claims claims = jws.getBody();

            String userId = claims.get("userId").toString();
            String jti = jws.getBody().getId();

            if (type.equals(ACCESS_TOKEN_SUBJECT)){
                if (jti == null || userId == null || tokenService.isAccessTokenInvalid(userId, jti)) {
                    throw new TokenExpiredException();
                }
            } else {
                if (jti == null || userId == null || tokenService.isRefreshTokenInvalid(userId, jti)) {
                    throw new TokenExpiredException();
                }
            }

            return jws;
        } catch (ExpiredJwtException e) {
            log.info(e.getMessage());
            throw new TokenExpiredException();
        } catch (JwtException e) {
            log.info(e.getMessage());
            throw new InvalidTokenException();
        }
    }

    public JwtToken reissue(String refreshToken) {
        String userId = getUserIdFromToken(refreshToken);

        Users user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(UserNotFoundException::new);

        return JwtToken.builder()
                .accessToken(createAccessToken(user))
                .refreshToken(refreshRefreshToken(user))
                .build();
    }

    public String getUserIdFromToken(String token) {
        Jws<Claims> claims = validateToken(token, REFRESH_TOKEN_SUBJECT);

        return claims.getBody().get("userId").toString();
    }
}
