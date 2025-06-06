package com.yaxim.dashboard.controller;

import com.yaxim.dashboard.controller.dto.response.TeamsTeamActivityDetailResponse;
import com.yaxim.dashboard.controller.dto.response.TeamsUserActivityCountsResponse;
import com.yaxim.dashboard.controller.dto.response.TeamsUserActivityUserDetailResponse;
import com.yaxim.dashboard.service.TeamsAnalyticsService;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "대시보드 기능")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class TeamsAnalyticsController {

    private final TeamsAnalyticsService teamsAnalyticsService;

    @GetMapping("/TeamsUserActivityCounts")
    public ResponseEntity<List<TeamsUserActivityCountsResponse>> getTeamsUserActivityCounts(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamsUserActivityCounts(auth.getUserId()));
    }

    @GetMapping("/TeamsUserActivityUserDetail")
    public ResponseEntity<List<TeamsUserActivityUserDetailResponse>> getTeamsUserActivityUserDetail(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamsUserActivityUserDetail(auth.getUserId()));
    }

    @GetMapping("/TeamsTeamActivityDetail")
    public ResponseEntity<List<TeamsTeamActivityDetailResponse>> getTeamsTeamActivityDetail(
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(teamsAnalyticsService.getTeamsTeamActivityDetail(auth.getUserId()));
    }
}