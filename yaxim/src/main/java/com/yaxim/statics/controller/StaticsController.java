package com.yaxim.statics.controller;

import com.yaxim.global.auth.aop.CheckRole;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.statics.controller.dto.response.AverageStaticsResponse;
import com.yaxim.statics.controller.dto.response.GeneralStaticsResponse;
import com.yaxim.statics.service.TeamStaticsService;
import com.yaxim.statics.service.UserStaticsService;
import com.yaxim.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statics")
@RequiredArgsConstructor
public class StaticsController {
    private final UserStaticsService userStaticsService;
    private final TeamStaticsService teamStaticsService;

    @GetMapping("/user")
    public ResponseEntity<List<GeneralStaticsResponse>> getUserStatics(JwtAuthentication auth) {
        return ResponseEntity.ok(userStaticsService.getUserStatic(auth.getUserId()));
    }

    @GetMapping("/user/avg")
    public ResponseEntity<List<AverageStaticsResponse>> getUserStatics() {
        return ResponseEntity.ok(userStaticsService.getUsersAverageStatic());
    }

    @CheckRole(UserRole.LEADER)
    @GetMapping("/team")
    public ResponseEntity<List<GeneralStaticsResponse>> getTeamStatics(JwtAuthentication auth) {
        return ResponseEntity.ok(teamStaticsService.getTeamStatic(auth.getUserId()));
    }

    @CheckRole(UserRole.LEADER)
    @GetMapping("/team/avg")
    public ResponseEntity<List<AverageStaticsResponse>> getTeamStatics() {
        return ResponseEntity.ok(teamStaticsService.getTeamsAverageStatic());
    }

}
