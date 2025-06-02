package com.yaxim.project.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 수정 요청 (Multipart 지원)")
public class MultipartProjectUpdateRequest {

    @Size(min = 1, max = 200, message = "프로젝트명은 1자 이상 200자 이하여야 합니다.")
    @Schema(description = "프로젝트명", example = "수정된 웹 애플리케이션 개발")
    private String name;

    @Schema(description = "팀 ID", example = "1")
    private Long teamId;

    @Schema(description = "프로젝트 시작일", example = "2024-01-01T09:00:00")
    private LocalDateTime startDate;

    @Schema(description = "프로젝트 종료일", example = "2024-12-31T18:00:00")
    private LocalDateTime endDate;

    @Size(max = 1000, message = "프로젝트 설명은 1000자 이하여야 합니다.")
    @Schema(description = "프로젝트 설명", example = "수정된 프로젝트 설명")
    private String description;

    @Schema(description = "새로 업로드할 파일들")
    private List<MultipartFile> newFiles;

    @Schema(description = "삭제할 기존 파일 ID 목록", example = "[1, 3, 5]")
    private List<Long> deleteFileIds;

    // 헬퍼 메서드들
    @JsonIgnore
    public boolean hasNewFiles() {
        return newFiles != null && !newFiles.isEmpty() && 
               newFiles.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    @JsonIgnore
    public List<MultipartFile> getValidNewFiles() {
        if (newFiles == null) return List.of();
        return newFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    @JsonIgnore
    public boolean hasFilesToDelete() {
        return deleteFileIds != null && !deleteFileIds.isEmpty();
    }

    @JsonIgnore
    public List<Long> getValidDeleteFileIds() {
        if (deleteFileIds == null) return List.of();
        return deleteFileIds.stream()
                .filter(id -> id != null && id > 0)
                .toList();
    }

    // 필드별 수정 여부 확인
    @JsonIgnore
    public boolean hasNameUpdate() {
        return name != null && !name.trim().isEmpty();
    }

    @JsonIgnore
    public boolean hasTeamIdUpdate() {
        return teamId != null;
    }

    @JsonIgnore
    public boolean hasDateUpdate() {
        return startDate != null || endDate != null;
    }

    @JsonIgnore
    public boolean hasDescriptionUpdate() {
        return description != null;
    }
}
