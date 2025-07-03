package com.yaxim.report.controller.dto.response;

import com.yaxim.report.entity.UserDailyReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class DailyReportResponse {
    @Schema(description = "보고서 ID")
    private Long id;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "보고서 생성일")
    private LocalDate date;

    @Schema(description = "보고서 제목")
    private String title;

    @Schema(description = "보고서 미리보기")
    private String preview;

    public static DailyReportResponse from(UserDailyReport report) {
        Map<String, Object> reportMap = report.getReport();

        String reportTitle = (String) reportMap.getOrDefault("report_title", "");
        // 중첩된 Map 꺼내기
        Map<String, Object> dailyReportMap = (Map<String, Object>) reportMap.get("daily_report");
        String summary = (String) dailyReportMap.getOrDefault("summary", "");

        return new DailyReportResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getDate(),
                reportTitle,
                summary
        );
    }
}
