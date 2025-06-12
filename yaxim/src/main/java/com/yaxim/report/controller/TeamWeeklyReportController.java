package com.yaxim.report.controller;

import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.report.controller.dto.request.ReportCreateRequest;
import com.yaxim.report.controller.dto.response.ReportResponse;
import com.yaxim.report.service.TeamWeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "팀 위클리 보고서")
@RestController
@RequestMapping("/reports/team/weekly")
@RequiredArgsConstructor
public class TeamWeeklyReportController {

  private final TeamWeeklyReportService teamWeeklyReportService; // 주입 필요

 @Operation(summary = "[리더] 팀 위클리 보고서 생성")
 @PostMapping
 public ResponseEntity<ReportResponse> createTeamWeeklyReport(
         @Valid @RequestBody ReportCreateRequest request,
         @Parameter(hidden = true) JwtAuthentication auth) {
   ReportResponse response = teamWeeklyReportService.createTeamWeeklyReport(auth.getUserId(), request);
   return ResponseEntity.status(HttpStatus.CREATED).body(response);
 }

 @Operation(summary = "[리더] 팀 위클리 보고서 목록 조회")
 @GetMapping
 public ResponseEntity<Page<ReportResponse>> getTeamWeeklyReports(
         @Parameter(description = "조회 시작일", example = "2025-06-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
         @Parameter(description = "조회 종료일", example = "2025-06-07") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
         @Parameter(hidden = true) JwtAuthentication auth,
         @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
   Page<ReportResponse> reports = teamWeeklyReportService.getTeamWeeklyReports(auth.getUserId(), start, end, pageable);
   return ResponseEntity.ok(reports);
 }

 @Operation(summary = "[리더] 팀 위클리 보고서 상세 조회")
 @GetMapping("/{reportId}")
 public ResponseEntity<ReportResponse> getTeamWeeklyReport(
         @PathVariable Long reportId,
         @Parameter(hidden = true) JwtAuthentication auth) {
   ReportResponse report = teamWeeklyReportService.getReportById(reportId, auth.getUserId());
   return ResponseEntity.ok(report);
 }

 @Operation(summary = "[리더] 팀 위클리 보고서 삭제")
 @DeleteMapping("/{reportId}")
 public ResponseEntity<Void> deleteTeamWeeklyReport(
         @PathVariable Long reportId,
         @Parameter(hidden = true) JwtAuthentication auth) {
   teamWeeklyReportService.deleteReport(reportId, auth.getUserId());
   return ResponseEntity.noContent().build();
 }
}
