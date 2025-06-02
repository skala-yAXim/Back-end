package com.yaxim.project.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 수정 요청")
public class ProjectUpdateRequest {

    @Size(min = 1, max = 200, message = "프로젝트명은 1자 이상 200자 이하여야 합니다.")
    @Schema(description = "프로젝트명", example = "웹 애플리케이션 개발")
    private String name;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "프로젝트 시작일", example = "2024-01-01T09:00:00")
    private LocalDateTime startDate;

    @Schema(description = "프로젝트 종료일", example = "2024-12-31T18:00:00")
    private LocalDateTime endDate;

    @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
    @Schema(description = "프로젝트 설명", example = "React와 Spring Boot를 사용한 웹 애플리케이션 개발 프로젝트")
    private String description;

    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }

    public boolean hasTeamId() {
        return teamId != null;
    }

    public boolean hasStartDate() {
        return startDate != null;
    }

    public boolean hasEndDate() {
        return endDate != null;
    }

    public boolean hasDescription() {
        return description != null;
    }
}
