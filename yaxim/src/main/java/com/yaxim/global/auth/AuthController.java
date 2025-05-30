package com.yaxim.global.auth;

import com.yaxim.global.auth.jwt.JwtProvider;
import com.yaxim.global.auth.jwt.JwtToken;
import com.yaxim.global.auth.jwt.TokenService;
import com.yaxim.global.auth.jwt.exception.InvalidTokenException;
import com.yaxim.global.auth.jwt.exception.TokenExpiredException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "JWT 인증 및 재발급 API (설명이 있는 API 외에는 Swagger JWT 설정이 필요하지 않습니다)")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final CookieService cookieService;

    @Operation(summary = "Access Token 유효성 및 만료 유무 확인 (AccessToken으로 Swagger JWT 설정 후 테스트해주세요.)", description = "쿠키에 저장된 Access Token이 유효한지 확인합니다. (개발용으로 Access Token의 Duration을 10초로 설정해두었습니다.)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 유효함"),
            @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음 또는 없음")
    })
    @GetMapping("/isValid")
    public ResponseEntity<Void> isValid(HttpServletRequest request) {
        String accessToken = cookieService.getAccessTokenFromCookie(request);

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        jwtProvider.validateToken(accessToken, "AccessToken");

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @Operation(summary = "로그아웃", description = "AccessToken과 RefreshToken을 모두 무효화하고 쿠키를 제거합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정상적으로 로그아웃됨"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Access Token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = cookieService.getAccessTokenFromCookie(request);
        String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        try {
            Jws<Claims> claims = jwtProvider.validateToken(refreshToken, "RefreshToken");
            String userId = claims.getBody().get("userId").toString();

            tokenService.invalidateRefreshToken(userId);

            if (accessToken != null) {
                tokenService.invalidateAccessToken(userId);
            }

            cookieService.deleteCookie(response);

            return ResponseEntity.ok().build();

        } catch (InvalidTokenException | TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Operation(summary = "AccessToken 재발급", description = "RefreshToken을 통해 새로운 AccessToken 및 RefreshToken을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "Refresh Token이 유효하지 않거나 만료됨")
    })
    @PostMapping("/reissue")
    public ResponseEntity<Void> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookie(request);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            JwtToken newToken = jwtProvider.reissue(refreshToken);

            cookieService.setCookie(
                    response,
                    newToken.getAccessToken(),
                    newToken.getRefreshToken()
            );

            return ResponseEntity.status(HttpStatus.OK).build();

        } catch (InvalidTokenException | TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
