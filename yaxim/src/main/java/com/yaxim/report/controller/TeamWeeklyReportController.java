package com.yaxim.report.controller;

import com.yaxim.global.auth.aop.CheckRole;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.report.controller.dto.request.TeamMemberWeeklyPageRequest;
import com.yaxim.report.controller.dto.response.TeamMemberWeeklyDetailResponse;
import com.yaxim.report.controller.dto.response.TeamMemberWeeklyReportResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportDetailResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportResponse;
import com.yaxim.report.service.TeamWeeklyReportService;
import com.yaxim.user.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CheckRole(UserRole.LEADER)
@Tag(name = "보고서 - 팀 Weekly 및 팀 멤버 Weekly [팀장 기능]")
@RestController
@RequestMapping("/reports/team/weekly")
@RequiredArgsConstructor
public class TeamWeeklyReportController {

    private final TeamWeeklyReportService teamWeeklyReportService; // 주입 필요

    @Operation(summary = "팀 멤버 보고서 목록 조회")
    @GetMapping("/member")
    public ResponseEntity<Page<TeamMemberWeeklyReportResponse>> getTeamMemberWeeklyReports(
            @RequestBody TeamMemberWeeklyPageRequest request,
            Pageable pageable,
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(
                teamWeeklyReportService.getTeamMemberWeeklyReports(
                        request,
                        pageable,
                        auth.getUserId()
                )
        );
    }

    @Operation(summary = "팀 멤버 보고서 상세 조회")
    @GetMapping("/member/{reportId}")
    public ResponseEntity<TeamMemberWeeklyDetailResponse> getTeamMemberWeeklyReport(
            @PathVariable("reportId") Long reportId,
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(
                teamWeeklyReportService.getTeamMemberWeeklyReport(
                        reportId,
                        auth.getUserId()
                )
        );
    }

    @Operation(summary = "팀 위클리 보고서 목록 조회")
    @GetMapping
    public ResponseEntity<Page<WeeklyReportResponse>> getTeamWeeklyReports(
            @Parameter(hidden = true) JwtAuthentication auth,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<WeeklyReportResponse> reports = teamWeeklyReportService.getTeamWeeklyReport(auth.getUserId(), pageable);
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "팀 위클리 보고서 상세 조회")
    @GetMapping("/{reportId}")
    public ResponseEntity<WeeklyReportDetailResponse> getTeamWeeklyReport(
            @PathVariable Long reportId,
            @Parameter(hidden = true) JwtAuthentication auth
    ) {
        WeeklyReportDetailResponse report = teamWeeklyReportService.getReportById(reportId, auth.getUserId());
        return ResponseEntity.ok(report);
    }

    @Operation(summary = "팀 위클리 보고서 삭제")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteTeamWeeklyReport(
            @PathVariable Long reportId,
            @Parameter(hidden = true) JwtAuthentication auth
    ) {
        teamWeeklyReportService.deleteReport(reportId, auth.getUserId());
        return ResponseEntity.noContent().build();
    }
}
