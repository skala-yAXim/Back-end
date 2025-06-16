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
public class TeamMemberWeeklyDetailResponse {
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

    @Schema(description = "보고서 내용 (JSON 객체)")
    private Object report;

    @Schema(description = "작성자 ID (개인 보고서만 해당)")
    private Long userId;

    @Schema(description = "작성자 이름 (개인 보고서만 해당)")
    private String userName;

    public static TeamMemberWeeklyDetailResponse from(UserWeeklyReport report) {
        Map<String, Object> reportMap = report.getReport();
        Users user = report.getUser();

        return new TeamMemberWeeklyDetailResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getUpdatedAt(),
                report.getStartDate(),
                report.getEndDate(),
                (String) reportMap.getOrDefault("title", ""),
                reportMap,
                user.getId(),
                user.getName()
        );
    }
}
