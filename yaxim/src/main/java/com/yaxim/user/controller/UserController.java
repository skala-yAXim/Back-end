package com.yaxim.user.controller;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.user.controller.dto.response.UserInfoResponse;
import com.yaxim.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "유저 정보 관리", description = "유저 정보를 저장, 수정, 삭제합니다.")
@RestController
@RequestMapping("/my")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponse> getMyInfo(JwtAuthentication auth) {
        return ResponseEntity.ok(
                userService.getUserInfo(auth.getUserId())
        );
    }
}
