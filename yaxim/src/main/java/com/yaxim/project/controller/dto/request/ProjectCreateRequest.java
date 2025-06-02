package com.yaxim.project.controller.dto.request;

import com.yaxim.project.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * JSON 방식 프로젝트 생성 요청 DTO
 * application/json Content-Type으로 사용
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "프로젝트 생성 요청 (JSON)")
public class ProjectCreateRequest extends BaseProjectRequest {

    /**
     * 엔티티 변환 메서드
     * @return Project 엔티티 객체
     */
    public Project toEntity() {
        return Project.builder()
                .name(this.getTrimmedName())
                .teamId(this.getTeamId())
                .startDate(this.getStartDate())
                .endDate(this.getEndDate())
                .description(this.getDescription())
                .build();
    }
}
