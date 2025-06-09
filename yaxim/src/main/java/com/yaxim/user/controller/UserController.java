package com.yaxim.user.controller;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.user.controller.dto.request.UserInfoRequest;
import com.yaxim.user.controller.dto.response.UserInfoResponse;
import com.yaxim.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저 정보 관리", description = "유저 정보를 저장, 수정, 삭제합니다.")
@RestController
@RequestMapping("/my")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "유저 정보 조회", description = "유저 아이디(userId), 이름(name), 이메일(email), 팀 내 역할(userRole), 깃 이메일(gitEmail) 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 유효함"),
            @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음 또는 없음"),
            @ApiResponse(responseCode = "404", description = "유저 정보를 찾을 수 없음")
    })
    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getMyInfo(JwtAuthentication auth) {
        return ResponseEntity.ok(
                userService.getUserInfo(auth.getUserId())
        );
    }

    @Operation(summary = "유저 정보 수정", description = "이름(name), 깃 이메일(gitEmail) 정보를 수정합니다. PatchMapping 이기 때문에 수정하지 않을 항목은 request에 포함하지 않아도 됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 유효함"),
            @ApiResponse(responseCode = "401", description = "토큰 유효하지 않음 또는 없음"),
            @ApiResponse(responseCode = "404", description = "유저 정보를 찾을 수 없음")
    })
    @PatchMapping
    public ResponseEntity<UserInfoResponse> updateMyInfo(
            @RequestBody @Validated UserInfoRequest request,
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(
                userService.updateUserInfo(request, auth.getUserId())
        );
    }
}
