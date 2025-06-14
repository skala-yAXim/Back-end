package com.yaxim.report.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
