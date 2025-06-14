package com.yaxim.report.controller.dto.response;

import com.yaxim.report.entity.UserDailyReport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class DailyReportDetailResponse {
    @Schema(description = "보고서 ID")
    private Long id;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "보고서 생성")
    private LocalDate date;

    @Schema(description = "보고서 제목")
    private String title;

    @Schema(description = "보고서 내용 (Json)")
    private Object report;

    public static DailyReportDetailResponse from(UserDailyReport report) {
        Map<String, Object> reportMap = report.getReport();

        return new DailyReportDetailResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getDate(),
                (String) reportMap.getOrDefault("report_title", ""),
                reportMap
        );
    }
}
