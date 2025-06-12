package com.yaxim.report.controller;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.report.controller.dto.request.ReportCreateRequest;
import com.yaxim.report.controller.dto.response.ReportResponse;
import com.yaxim.report.service.UserWeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "개인 위클리 보고서")
@RestController
@RequestMapping("/reports/user/weekly")
@RequiredArgsConstructor
public class UserWeeklyReportController {

     private final UserWeeklyReportService weeklyReportService; // 주입 필요

    @Operation(summary = "내 위클리 보고서 생성")
    @PostMapping
    public ResponseEntity<ReportResponse> createMyWeeklyReport(
            @Valid @RequestBody ReportCreateRequest request) {
         ReportResponse response = weeklyReportService.createWeeklyReport(request.getUserId(), request);
         return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "내 위클리 보고서 목록 조회")
    @GetMapping
    public ResponseEntity<Page<ReportResponse>> getMyWeeklyReports(
            @Parameter(hidden = true) JwtAuthentication auth,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
         Page<ReportResponse> reports = weeklyReportService.getMyWeeklyReports(auth.getUserId(), pageable);
         return ResponseEntity.ok(reports);
    }

    @Operation(summary = "내 위클리 보고서 상세 조회")
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportResponse> getMyWeeklyReport(
            @PathVariable Long reportId,
            @Parameter(hidden = true) JwtAuthentication auth) {
         ReportResponse report = weeklyReportService.getReportById(reportId, auth.getUserId());
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

