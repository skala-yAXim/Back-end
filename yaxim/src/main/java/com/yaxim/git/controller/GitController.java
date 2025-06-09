package com.yaxim.git.controller;

import com.yaxim.git.controller.dto.request.GitWebhookRequest;
import com.yaxim.git.controller.dto.response.GitInfoResponse;
import com.yaxim.git.service.GitInfoService;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/git")
@RequiredArgsConstructor
public class GitController {
    private final GitInfoService gitInfoService;

    @PostMapping("/webhook")
    public void webhook(@RequestBody GitWebhookRequest request) {
        gitInfoService.updateTeamGitInfo(request);
    }

    @GetMapping("/my")
    public ResponseEntity<GitInfoResponse> getMyGitInfo(JwtAuthentication auth) {
        return ResponseEntity.ok(
                gitInfoService.getGitInfo(auth.getUserId())
        );
    }

    @DeleteMapping
    public void deleteGitInfo(JwtAuthentication auth) {
        gitInfoService.deleteGitInfo(auth.getUserId());
    }
}
