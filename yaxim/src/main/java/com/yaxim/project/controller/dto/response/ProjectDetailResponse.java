package com.yaxim.project.controller.dto.response;

import com.yaxim.project.entity.Project;
import com.yaxim.project.entity.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 응답")
public class ProjectDetailResponse {

    @Schema(description = "프로젝트 ID", example = "1")
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Schema(description = "프로젝트명", example = "웹 애플리케이션 개발")
    private String name;

    @Schema(description = "프로젝트 시작일", example = "2024-01-01")
    private LocalDate startDate;

    @Schema(description = "프로젝트 종료일", example = "2024-12-31")
    private LocalDate endDate;

    @Schema(description = "프로젝트 설명", example = "React와 Spring Boot를 사용한 웹 애플리케이션 개발 프로젝트")
    private String description;

    @Schema(description = "프로젝트 상태", example = "enum(PLANNING, IN_PROGRESS, COMPLETED)")
    private ProjectStatus status;

    @Schema(description = "프로젝트 파일 목록")
    private List<ProjectFileResponse> files;

    public static ProjectDetailResponse from(Project project) {
        return ProjectDetailResponse.builder()
                .id(project.getId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .name(project.getName())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .description(project.getDescription())
                .status(project.calculateStatus()) // 팀장님 요구사항: 동적 상태 계산
                .files(project.getProjectFiles().stream()
                        .map(ProjectFileResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
