package com.yaxim.project.controller.dto.response;

import com.yaxim.project.entity.ProjectFile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "프로젝트 파일 응답")
public class ProjectFileResponse {

    @Schema(description = "파일 ID", example = "1")
    private Long id;

    @Schema(description = "원본 파일명", example = "프로젝트_계획서.pdf")
    private String originalFileName;

    @Schema(description = "파일 URL", example = "https://cdn.example.com/files/uuid-filename.pdf")
    private String fileUrl;

    public static ProjectFileResponse from(ProjectFile projectFile) {
        return ProjectFileResponse.builder()
                .id(projectFile.getId())
                .originalFileName(projectFile.getOriginalFileName())
                .fileUrl(projectFile.getFileUrl())
                .build();
    }
}
