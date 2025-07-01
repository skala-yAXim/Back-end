package com.yaxim.global.auth.jwt;

import com.yaxim.global.auth.jwt.exception.InvalidTokenException;
import com.yaxim.global.auth.jwt.exception.TokenExpiredException;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import com.yaxim.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {

    @InjectMocks
    private JwtProvider provider;

    @Mock
    private JwtSecret secrets;

    @Mock
    private TokenService tokenService;
    @Mock
    private UserRepository userRepository;

    private Users user;

    private static final Long USER_ID = 1L;
    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final Long ACCESS_TOKEN_EXPIRED_BY = 60L * 10L;
    private static final Long REFRESH_TOKEN_EXPIRED_BY = 60L * 60L * 24L * 7L;


    @BeforeEach
    void setUp() {
        when(secrets.getSecret()).thenReturn("test-secret-which-is-long-enough-for-HMAC");

        provider.init();

        user = new Users(
                USER_ID,
                "name",
                "email"
        );
        user.setUserRole(UserRole.USER);
    }

    @Test
    @DisplayName("Access Token 발급")
    public void testCreateAccessToken() {
        // given
        when(secrets.getAccessTokenValidityInSeconds()).thenReturn(ACCESS_TOKEN_EXPIRED_BY);
        String accessToken = provider.createAccessToken(user);
        assertNotNull(accessToken);

        // when
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(secrets.getSecret().getBytes())
                .build()
                .parseClaimsJws(accessToken);

        // then
        assertEquals(user.getId().toString(), claims.getBody().get("userId").toString());
        verify(tokenService)
                .storeAccessTokenJti(
                        anyString(),
                        anyString(),
                        any(Duration.class)
                );
    }



    @Test
    @DisplayName("Refresh Token 발급")
    void testCreateRefreshToken() {
        // given
        when(secrets.getRefreshTokenValidityInSeconds()).thenReturn(REFRESH_TOKEN_EXPIRED_BY);
        String refreshToken = provider.createRefreshToken(user);
        assertNotNull(refreshToken);

        // when
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(secrets.getSecret().getBytes())
                .build()
                .parseClaimsJws(refreshToken);

        // then
        assertEquals(user.getId().toString(), claims.getBody().get("userId").toString());
        verify(tokenService)
                .storeRefreshTokenJti(
                        anyString(),
                        anyString(),
                        any(Duration.class)
                );
    }

    @Test
    @DisplayName("Token 검증")
    void validateToken() {
        // given
        when(secrets.getAccessTokenValidityInSeconds()).thenReturn(ACCESS_TOKEN_EXPIRED_BY);
        when(secrets.getRefreshTokenValidityInSeconds()).thenReturn(REFRESH_TOKEN_EXPIRED_BY);
        String accessToken = provider.createAccessToken(user);
        String refreshToken = provider.createRefreshToken(user);

        // when & then
        assertDoesNotThrow(() -> provider.validateToken(accessToken, REFRESH_TOKEN_SUBJECT));
        assertDoesNotThrow(() -> provider.validateToken(refreshToken, ACCESS_TOKEN_SUBJECT));
    }

    @Test
    @DisplayName("Refresh Token 발급 실패 - user null")
    void testCreateRefreshToken_NullUser() {
        // given
        Users nullUser = null;

        // when & then
        assertThrows(NullPointerException.class, () -> provider.createRefreshToken(nullUser));
    }

    @Test
    @DisplayName("Access Token 만료 예외처리")
    void validateToken_expired() {
        // given
        when(secrets.getAccessTokenValidityInSeconds()).thenReturn(-1L);
        String token = provider.createAccessToken(user);

        // when & then
        assertThrows(TokenExpiredException.class, () -> provider.validateToken(token, ACCESS_TOKEN_SUBJECT));
    }

    @Test
    @DisplayName("Refresh Token 만료 예외처리")
    void validateToken_with_expired_refresh_token() {
        // given
        when(secrets.getRefreshTokenValidityInSeconds()).thenReturn(-1L);
        String token = provider.createRefreshToken(user);

        // when & then
        assertThrows(TokenExpiredException.class, () -> provider.validateToken(token, REFRESH_TOKEN_SUBJECT));
    }

    @Test
    @DisplayName("유효하지 않은 Access Token 예외처리")
    void validateToken_invalid() {
        // given
        String token = "invalid-access-token";

        // when & then
        assertThrows(InvalidTokenException.class, () -> provider.validateToken(token, ACCESS_TOKEN_SUBJECT));
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token 예외처리")
    void validateToken_with_invalid_refresh_token() {
        // given
        String token = "invalid-refresh-token";

        // when & then
        assertThrows(InvalidTokenException.class, () -> provider.validateToken(token, REFRESH_TOKEN_SUBJECT));
    }

    @Test
    @DisplayName("Null 값 예외처리")
    void validateToken_invalid_null_string() {
        // given & when & then
        assertThrows(InvalidTokenException.class, () -> // then
                provider.validateToken( // when
                        null, // given
                        ACCESS_TOKEN_SUBJECT
                )
        );
    }

    @Test
    @DisplayName("토큰 재발급")
    void reissueAccessAndRefreshTokens() {
        // given
        when(secrets.getAccessTokenValidityInSeconds()).thenReturn(ACCESS_TOKEN_EXPIRED_BY);
        when(secrets.getRefreshTokenValidityInSeconds()).thenReturn(REFRESH_TOKEN_EXPIRED_BY);
        when(userRepository.findById(user.getId())).thenReturn(Optional.ofNullable(user));
        String originalRefreshToken = provider.createRefreshToken(user);

        // when
        JwtToken jwtToken = provider.reissue(originalRefreshToken);

        // then
        assertNotNull(jwtToken);
        assertNotNull(jwtToken.getAccessToken());
        assertNotNull(jwtToken.getRefreshToken());

        Jws<Claims> accessJws = Jwts.parserBuilder()
                .setSigningKey(secrets.getSecret().getBytes())
                .build()
                .parseClaimsJws(jwtToken.getAccessToken());

        Jws<Claims> refreshJws = Jwts.parserBuilder()
                .setSigningKey(secrets.getSecret().getBytes())
                .build()
                .parseClaimsJws(jwtToken.getRefreshToken());

        assertEquals(user.getId().toString(), accessJws.getBody().get("userId").toString());
        assertEquals(user.getId().toString(), refreshJws.getBody().get("userId").toString());

        verify(tokenService)
                .storeAccessTokenJti(
                        anyString(),
                        anyString(),
                        any(Duration.class)
                );
        verify(tokenService)
                .storeRefreshTokenJti(
                        anyString(),
                        anyString(),
                        any(Duration.class)
                );
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token으로 재발급 시 예외처리")
    void reissueInvalidTokenException() {
        // given
        String invalidRefreshToken = "invalid-refresh-token";

        // when & then
        assertThrows(InvalidTokenException.class, () -> provider.reissue(invalidRefreshToken));
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 재발급 시 예외처리")
    void reissueTokenExpiredException() {
        // given
        when(secrets.getRefreshTokenValidityInSeconds()).thenReturn(-1L);
        String expiredRefreshToken = provider.createRefreshToken(user);

        // when & then
        assertThrows(TokenExpiredException.class, () -> provider.reissue(expiredRefreshToken));
    }

    @Test
    @DisplayName("Null 값으로 재발급 시 예외처리")
    void reissueNullPointerException() {
        // given & when & then
        assertThrows(InvalidTokenException.class, () -> provider.reissue(null));
    }
}
