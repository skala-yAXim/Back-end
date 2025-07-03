package com.yaxim.git.controller;

import com.yaxim.git.controller.dto.request.GitWebhookRequest;
import com.yaxim.git.controller.dto.response.GitInfoResponse;
import com.yaxim.git.service.GitInfoService;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/git")
@RequiredArgsConstructor
@Tag(name = "Git 연동", description = "연동된 Git 정보를 조회/삭제하거나, Git Webhook 전용 URL이 있습니다.")
public class GitController {
    private final GitInfoService gitInfoService;

    @PostMapping("/webhook")
    @Operation(summary = "[팀장 기능] Git Webhook 전용 URL, 팀장이 보낸 Webhook이 아니면 예외처리")
    public void webhook(@RequestBody GitWebhookRequest request) {
        gitInfoService.updateTeamGitInfo(request);
    }

    @GetMapping("/my")
    @Operation(summary = "연동된 Git 정보 조회")
    public ResponseEntity<GitInfoResponse> getMyGitInfo(JwtAuthentication auth) {
        return ResponseEntity.ok(
                gitInfoService.getGitInfo(auth.getUserId())
        );
    }

    @DeleteMapping
    @Operation(summary = "연동된 Git 정보 삭제")
    public void deleteGitInfo(JwtAuthentication auth) {
        gitInfoService.deleteGitInfo(auth.getUserId());
    }
}
