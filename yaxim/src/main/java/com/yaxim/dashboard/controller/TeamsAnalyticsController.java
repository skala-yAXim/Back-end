package com.yaxim.dashboard.controller;

import com.yaxim.dashboard.controller.dto.response.*;
import com.yaxim.dashboard.service.TeamsAnalyticsService;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "대시보드 기능", description = "팀 및 개인 대시보드 기능")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class TeamsAnalyticsController {

    private final TeamsAnalyticsService teamsAnalyticsService;

    @GetMapping("/TeamsUserActivityCounts")
    public ResponseEntity<TeamsUserActivityCountsResponse> getTeamsUserActivityCounts(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamsUserActivityCounts(auth.getUserId()));
    }

    @GetMapping("/TeamsUserActivityUserDetail")
    public ResponseEntity<TeamsUserActivityUserDetailResponse> getTeamsUserActivityUserDetail(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamsUserActivityUserDetail(auth.getUserId()));
    }

    @GetMapping("/TeamsTeamActivityDetail")
    public ResponseEntity<TeamsTeamActivityDetailResponse> getTeamsTeamActivityDetail(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamsTeamActivityDetail(auth.getUserId()));
    }

    @GetMapping("/TeamsTeamActivityCounts")
    public ResponseEntity<TeamsTeamActivityCountsResponse> getTeamsTeamActivityCounts(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamsTeamActivityCounts(auth.getUserId()));
    }

    @GetMapping("/personal")
    public ResponseEntity<PersonalDashboardResponse> getPersonalDashboard(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getPersonalDashboard(auth.getUserId()));
    }

    @GetMapping("/team")
    public ResponseEntity<TeamDashboardResponse> getTeamDashboard(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamDashboard(auth.getUserId()));
    }
}