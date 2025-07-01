package com.yaxim.global.for_ai;

import com.yaxim.global.for_ai.dto.request.*;
import com.yaxim.global.for_ai.dto.response.TeamWithMemberAndProjectResponse;
import com.yaxim.report.controller.dto.response.DailyReportDetailResponse;
import com.yaxim.report.controller.dto.response.TeamWeeklyReportResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportDetailResponse;
import com.yaxim.report.service.TeamWeeklyReportService;
import com.yaxim.report.service.UserDailyReportService;
import com.yaxim.report.service.UserWeeklyReportService;
import com.yaxim.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api-for-ai")
@RequiredArgsConstructor
@Tag(name = "AI 전용 API", description = "자물쇠 눌러서 AI API Key로 Authorize 해주세요!")
public class ApiForAiController {
    private final TeamService teamService;
    private final UserDailyReportService userDailyReportService;
    private final UserWeeklyReportService userWeeklyReportService;
    private final TeamWeeklyReportService teamWeeklyReportService;

    // Team

    @GetMapping("/team-info")
    @Operation(summary = "DB에 저장되어 있는 모든 팀 조회")
    public ResponseEntity<List<TeamWithMemberAndProjectResponse>> getAllTeamsInfo() {
        return ResponseEntity.ok(teamService.getAllTeamsInfo());
    }

    // Report

    @Operation(summary = "개인 Daily 생성")
    @PostMapping("/report/daily")
    public ResponseEntity<DailyReportDetailResponse> createMyDailyReport(
            @Valid @RequestBody DailyReportCreateRequest request) {
        DailyReportDetailResponse response = userDailyReportService.createDailyReport(request.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "개인 Weekly 생성")
    @PostMapping("/report/user-weekly")
    public ResponseEntity<WeeklyReportDetailResponse> createMyWeeklyReport(
            @Valid @RequestBody WeeklyReportCreateRequest request) {
        WeeklyReportDetailResponse response = userWeeklyReportService.createWeeklyReport(request.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "팀 Weekly 생성")
    @PostMapping("/report/team-weekly")
    public ResponseEntity<TeamWeeklyReportResponse> createTeamWeeklyReport(
            @Valid @RequestBody TeamWeeklyReportCreateRequest request) {
        TeamWeeklyReportResponse response = teamWeeklyReportService.createTeamWeeklyReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "User ID로 개인 데일리 불러오기")
    @PostMapping("/user-daily")
    public ResponseEntity<List<DailyReportDetailResponse>> getMyDailyReport(
            @RequestBody UserDailyListRequest request
            ) {
        return ResponseEntity.ok(
                userDailyReportService.getUserDailyReport(request)
        );
    }

    @Operation(summary = "Team ID로 개인 위클리 불러오기")
    @PostMapping("/user-weekly")
    public ResponseEntity<List<WeeklyReportDetailResponse>> getMyWeeklyReport(
            @RequestBody UserWeeklyListRequest request
            ) {
        return ResponseEntity.ok(
                userWeeklyReportService.getUserWeeklyReport(request)
        );
    }
}
