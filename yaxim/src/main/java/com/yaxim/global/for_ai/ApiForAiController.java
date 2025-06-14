package com.yaxim.global.for_ai;

import com.yaxim.global.for_ai.dto.request.DailyReportCreateRequest;
import com.yaxim.global.for_ai.dto.request.WeeklyReportCreateRequest;
import com.yaxim.report.controller.dto.response.DailyReportDetailResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportDetailResponse;
import com.yaxim.report.service.TeamWeeklyReportService;
import com.yaxim.report.service.UserDailyReportService;
import com.yaxim.report.service.UserWeeklyReportService;
import com.yaxim.team.controller.dto.response.TeamResponse;
import com.yaxim.global.for_ai.dto.response.TeamWithMemberResponse;
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
    private final UserWeeklyReportService userWeeklyReportService;
    private final UserDailyReportService userDailyReportService;
    private final TeamWeeklyReportService teamWeeklyReportService;

    // Team

    @GetMapping("/team/all")
    @Operation(summary = "DB에 저장되어 있는 모든 팀 조회")
    public ResponseEntity<List<TeamResponse>> getAllTeams() {
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @GetMapping("/team/all/members")
    @Operation(summary = "DB에 저장되어 있는 모든 팀 및 팀 멤버 정보 조회")
    public ResponseEntity<List<TeamWithMemberResponse>> getTeamWithMemberResponses() {
        return ResponseEntity.ok(teamService.getTeamWithMemberResponses());
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
    public ResponseEntity<WeeklyReportDetailResponse> createTeamWeeklyReport(
            @Valid @RequestBody WeeklyReportCreateRequest request) {
        WeeklyReportDetailResponse response = teamWeeklyReportService.createTeamWeeklyReport(request.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // TODO Project (team id로 프로젝트 조회)

    // TODO LLM One Line Comment (DailyUserReport 테이블에 Comment 컬럼 추가)
}
