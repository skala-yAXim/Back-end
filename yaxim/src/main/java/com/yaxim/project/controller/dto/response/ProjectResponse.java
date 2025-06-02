package com.yaxim.project.controller.dto.response;

import com.yaxim.project.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 응답")
public class ProjectResponse {

    @Schema(description = "프로젝트 ID", example = "1")
    private Long id;

    @Schema(description = "프로젝트명", example = "웹 애플리케이션 개발")
    private String name;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "프로젝트 시작일", example = "2024-01-01T09:00:00")
    private LocalDateTime startDate;

    @Schema(description = "프로젝트 종료일", example = "2024-12-31T18:00:00")
    private LocalDateTime endDate;

    @Schema(description = "기간 (UI 표시용)", example = "2024-01-01 ~ 2024-12-31")
    private String date;

    @Schema(description = "프로젝트 설명", example = "React와 Spring Boot를 사용한 웹 애플리케이션 개발 프로젝트")
    private String description;

    @Schema(description = "프로젝트 상태 (동적 계산)", example = "진행중")
    private String status; // 동적으로 계산된 상태

    @Schema(description = "프로젝트 파일 목록")
    private List<ProjectFileResponse> files;

    public static ProjectResponse from(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .teamId(project.getTeamId())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .date(project.getDateRange()) // UI에서 요구하는 기간 표시
                .description(project.getDescription())
                .status(project.calculateStatus()) // 팀장님 요구사항: 동적 상태 계산
                .files(project.getProjectFiles().stream()
                        .map(ProjectFileResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
