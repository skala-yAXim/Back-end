package com.yaxim.report.controller.dto.response;

import com.yaxim.report.entity.UserWeeklyReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class WeeklyReportResponse {
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

    public static WeeklyReportResponse from(UserWeeklyReport report) {
        Map<String, Object> reportMap = report.getReport();

        String reportTitle = (String) reportMap.getOrDefault("report_title", "");

        return new WeeklyReportResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getStartDate(),
                report.getEndDate(),
                reportTitle
        );
    }
}
