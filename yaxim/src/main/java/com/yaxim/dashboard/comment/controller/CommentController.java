package com.yaxim.dashboard.comment.controller;

import com.yaxim.dashboard.comment.controller.dto.response.CommentResponse;
import com.yaxim.dashboard.comment.service.CommentService;
import com.yaxim.global.auth.aop.CheckRole;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.user.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @Operation(description = "유저의 Daily 한 줄 평 조회")
    @GetMapping("/user")
    public ResponseEntity<CommentResponse> getUserComment(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(commentService.getUserComment(auth.getUserId()));
    }

    @CheckRole(UserRole.LEADER)
    @Operation(description = "팀의 Weekly 한 줄 평 조회")
    @GetMapping("/team")
    public ResponseEntity<CommentResponse> getTeamComment(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(commentService.getTeamComment(auth.getUserId()));
    }

}
