package com.yaxim.project.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 프로젝트 요청 DTO의 공통 필드를 담는 베이스 클래스
 * JSON과 Multipart 요청에서 공통으로 사용되는 필드들을 정의
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "프로젝트 요청 베이스 DTO")
public abstract class BaseProjectRequest {

    @NotBlank(message = "프로젝트명은 필수입니다.")
    @Size(min = 1, max = 100, message = "프로젝트명은 1자 이상 100자 이하여야 합니다.")
    @Schema(description = "프로젝트명 (필수, 최대 100자)", example = "웹 애플리케이션 개발", required = true)
    private String name;

    @NotNull(message = "팀 ID는 필수입니다.")
    @Schema(description = "팀 ID (필수)", example = "1", required = true)
    private Long teamId;

    @NotNull(message = "프로젝트 시작일은 필수입니다.")
    @Schema(description = "프로젝트 시작일 (필수)", example = "2025-01-01T09:00:00", required = true)
    private LocalDateTime startDate;

    @NotNull(message = "프로젝트 종료일은 필수입니다.")
    @Schema(description = "프로젝트 종료일 (필수)", example = "2025-12-31T18:00:00", required = true)
    private LocalDateTime endDate;

    @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
    @Schema(description = "프로젝트 설명 (선택사항, 최대 1,000자)", example = "React와 Spring Boot를 사용한 웹 애플리케이션 개발 프로젝트")
    private String description;

    /**
     * 날짜 범위 검증 메서드
     * @return 시작일이 종료일보다 이후가 아니면 true
     */
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return false;
        return !endDate.isBefore(startDate);
    }

    /**
     * 프로젝트명을 트림해서 반환
     * @return 공백이 제거된 프로젝트명
     */
    public String getTrimmedName() {
        return name != null ? name.trim() : null;
    }
}
