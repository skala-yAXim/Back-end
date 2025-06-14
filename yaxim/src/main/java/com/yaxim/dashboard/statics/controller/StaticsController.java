package com.yaxim.dashboard.statics.controller;

import com.yaxim.global.auth.aop.CheckRole;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.dashboard.statics.controller.dto.response.AverageStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.GeneralStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.SumStaticResponse;
import com.yaxim.dashboard.statics.service.TeamStaticsService;
import com.yaxim.dashboard.statics.service.UserStaticsService;
import com.yaxim.user.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard/statics")
@RequiredArgsConstructor
@Tag(name = "업무 통계", description = "일별/일주일 개인/팀 업무량 통계를 반환합니다.")
public class StaticsController {
    private final UserStaticsService userStaticsService;
    private final TeamStaticsService teamStaticsService;

    @GetMapping("/user")
    @Operation(summary = "일별 개인 업무량 반환(총 7개)")
    public ResponseEntity<List<GeneralStaticsResponse>> getUserStatics(JwtAuthentication auth) {
        return ResponseEntity.ok(userStaticsService.getUserStatic(auth.getUserId()));
    }

    @GetMapping("/user/avg")
    @Operation(summary = "일별 전체 사용자의 평균 업무량 반환(총 7개)")
    public ResponseEntity<List<AverageStaticsResponse>> getUserStatics() {
        return ResponseEntity.ok(userStaticsService.getUsersAverageStatic());
    }

    @GetMapping("/user/week")
    @Operation(summary = "일주일 동안의 개인 업무량 반환(총 1개)")
    public ResponseEntity<SumStaticResponse> getUserWeekStatics(JwtAuthentication auth) {
        return ResponseEntity.ok(userStaticsService.getUserWeekStatics(auth.getUserId()));
    }

    @CheckRole(UserRole.LEADER)
    @GetMapping("/team")
    @Operation(summary = "[팀장 기능] 일별 팀 단위 업무량 반환(총 7개)")
    public ResponseEntity<List<GeneralStaticsResponse>> getTeamStatics(JwtAuthentication auth) {
        return ResponseEntity.ok(teamStaticsService.getTeamStatic(auth.getUserId()));
    }

    @CheckRole(UserRole.LEADER)
    @GetMapping("/team/avg")
    @Operation(summary = "[팀장 기능] 일별 팀 단위 평균 업무량 반환(총 7개)")
    public ResponseEntity<List<AverageStaticsResponse>> getTeamStatics() {
        return ResponseEntity.ok(teamStaticsService.getTeamsAverageStatic());
    }

    @CheckRole(UserRole.LEADER)
    @GetMapping("/team/week")
    @Operation(summary = "[팀장 기능] 일주일 동안의 팀 단위 업무량 반환(총 1개)")
    public ResponseEntity<SumStaticResponse> getWeekTeamStatics(JwtAuthentication auth) {
        return ResponseEntity.ok(teamStaticsService.getTeamWeekStatics(auth.getUserId()));
    }

}
