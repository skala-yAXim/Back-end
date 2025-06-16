package com.yaxim.report.controller.dto.response;

import com.yaxim.report.entity.UserWeeklyReport;
import com.yaxim.user.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@AllArgsConstructor
public class TeamMemberWeeklyReportResponse {
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

    @Schema(description = "보고서 미리보기")
    private String preview;

    @Schema(description = "작성자 ID (개인 보고서만 해당)")
    private Long userId;

    @Schema(description = "작성자 이름 (개인 보고서만 해당)")
    private String userName;

    public static TeamMemberWeeklyReportResponse fromTeam(UserWeeklyReport report) {
        Map<String, Object> reportMap = report.getReport();
        Users user = report.getUser();

        String reportTitle = (String) reportMap.getOrDefault("report_title", "");
        // 중첩된 Map 꺼내기
        Map<String, Object> weeklyReportMap = (Map<String, Object>) reportMap.get("weekly_report");
        String summary = (String) weeklyReportMap.getOrDefault("summary", "");

        return new TeamMemberWeeklyReportResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getStartDate(),
                report.getEndDate(),
                reportTitle,
                summary,
                user.getId(),
                user.getName()
        );
    }
}
