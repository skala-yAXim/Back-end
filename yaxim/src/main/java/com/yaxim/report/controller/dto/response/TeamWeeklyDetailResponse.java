package com.yaxim.report.controller.dto.response;

import com.yaxim.report.entity.TeamWeeklyReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class TeamWeeklyDetailResponse {
    @Schema(description = "보고서 ID")
    private Long id;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "보고서 시작일")
    private LocalDate startDate;

    @Schema(description = "보고서 종료일")
    private LocalDate endDate;

    @Schema(description = "보고서 제목")
    private String title;

    @Schema(description = "보고서 내용 (md 형식)")
    private String reportMd;

    @Schema(description = "보고서 내용 (JSON 객체)")
    private Object reportJson;

    public static TeamWeeklyDetailResponse from(TeamWeeklyReport report) {
        Map<String, Object> reportMap = report.getReport();

        return new TeamWeeklyDetailResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getStartDate(),
                report.getEndDate(),
                (String) reportMap.getOrDefault("report_title", ""),
                (String) reportMap.getOrDefault("weekly_report_md", ""),
                reportMap
        );
    }
}
