package com.yaxim.report.controller;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.report.controller.dto.response.WeeklyReportDetailResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportResponse;
import com.yaxim.report.service.UserWeeklyReportService;
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

@Tag(name = "개인 위클리 보고서")
@RestController
@RequestMapping("/reports/user/weekly")
@RequiredArgsConstructor
public class UserWeeklyReportController {

     private final UserWeeklyReportService weeklyReportService; // 주입 필요

    @Operation(summary = "내 위클리 보고서 목록 조회")
    @GetMapping
    public ResponseEntity<Page<WeeklyReportResponse>> getMyWeeklyReports(
            @Parameter(hidden = true) JwtAuthentication auth,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
         Page<WeeklyReportResponse> reports = weeklyReportService.getMyWeeklyReports(auth.getUserId(), pageable);
         return ResponseEntity.ok(reports);
    }

    @Operation(summary = "내 위클리 보고서 상세 조회")
    @GetMapping("/{reportId}")
    public ResponseEntity<WeeklyReportDetailResponse> getMyWeeklyReport(
            @PathVariable Long reportId,
            @Parameter(hidden = true) JwtAuthentication auth) {
         WeeklyReportDetailResponse report = weeklyReportService.getReportById(reportId, auth.getUserId());
         return ResponseEntity.ok(report);
    }

    @Operation(summary = "내 위클리 보고서 삭제")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteMyWeeklyReport(
            @PathVariable Long reportId,
            @Parameter(hidden = true) JwtAuthentication auth) {
         weeklyReportService.deleteReport(reportId, auth.getUserId());
         return ResponseEntity.noContent().build();
    }
}

