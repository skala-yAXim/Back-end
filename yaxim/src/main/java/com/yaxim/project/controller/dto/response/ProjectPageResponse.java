package com.yaxim.project.controller.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 페이징 응답")
public class ProjectPageResponse {

    @Schema(description = "프로젝트 목록")
    private List<ProjectResponse> projects;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int currentPage;

    @Schema(description = "페이지 크기", example = "10")
    private int pageSize;

    @Schema(description = "전체 요소 개수", example = "50")
    private long totalElements;

    @Schema(description = "전체 페이지 개수", example = "5")
    private int totalPages;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    private boolean isFirst;

    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean isLast;

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    private boolean hasNext;

    @Schema(description = "이전 페이지 존재 여부", example = "false")
    private boolean hasPrevious;

    public static ProjectPageResponse from(Page<ProjectResponse> page) {
        return ProjectPageResponse.builder()
                .projects(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
