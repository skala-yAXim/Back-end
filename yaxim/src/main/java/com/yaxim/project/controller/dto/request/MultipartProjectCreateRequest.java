package com.yaxim.project.controller.dto.request;

import com.yaxim.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Multipart 방식 프로젝트 생성 요청 DTO
 * multipart/form-data Content-Type으로 사용
 * 파일 업로드 기능 포함
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Schema(description = "프로젝트 생성 요청 (Multipart - 파일 포함)")
public class MultipartProjectCreateRequest extends BaseProjectRequest {

    @Schema(
        description = "업로드할 프로젝트 파일들 (최대 5개, DOCX/XLSX/TXT만 허용, 각 파일 최대 50MB)",
        type = "array",
        implementation = String.class,
        format = "binary"
    )
    private List<MultipartFile> files;

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

    // ========== 파일 관련 검증 메서드들 ==========

    /**
     * 파일 업로드 여부 확인
     * @return 유효한 파일이 하나라도 있으면 true
     */
    @JsonIgnore
    public boolean hasFiles() {
        return files != null && !files.isEmpty() && 
               files.stream().anyMatch(file -> file != null && !file.isEmpty());
    }

    /**
     * 유효한 파일들만 반환 (null이나 빈 파일 제외)
     * @return 유효한 파일 리스트
     */
    @JsonIgnore
    public List<MultipartFile> getValidFiles() {
        if (files == null) return List.of();
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    /**
     * 파일 개수 제한 검증 (최대 5개)
     * @return 파일이 5개 이하면 true
     */
    @JsonIgnore
    public boolean isFilesCountValid() {
        return getValidFiles().size() <= 5;
    }

    /**
     * 개별 파일 크기 검증 (각 파일 최대 50MB)
     * @return 모든 파일이 50MB 이하면 true
     */
    @JsonIgnore
    public boolean isEachFileSizeValid() {
        return getValidFiles().stream()
                .allMatch(file -> file.getSize() <= 50 * 1024 * 1024L); // 50MB
    }

    /**
     * 파일 형식 검증 (DOCX, XLSX, TXT만 허용)
     * @return 모든 파일이 허용된 형식이면 true
     */
    @JsonIgnore
    public boolean isFileFormatsValid() {
        return getValidFiles().stream()
                .allMatch(file -> {
                    String filename = file.getOriginalFilename();
                    if (filename == null) return false;
                    String extension = filename.toLowerCase();
                    return extension.endsWith(".docx") || 
                           extension.endsWith(".xlsx") || 
                           extension.endsWith(".txt");
                });
    }

    /**
     * 허용되지 않은 파일 형식 목록 반환
     * @return 잘못된 형식의 파일명 리스트
     */
    @JsonIgnore
    public List<String> getInvalidFileFormats() {
        return getValidFiles().stream()
                .filter(file -> {
                    String filename = file.getOriginalFilename();
                    if (filename == null) return true;
                    String extension = filename.toLowerCase();
                    return !extension.endsWith(".docx") && 
                           !extension.endsWith(".xlsx") && 
                           !extension.endsWith(".txt");
                })
                .map(MultipartFile::getOriginalFilename)
                .toList();
    }

    /**
     * 크기 초과 파일 목록 반환
     * @return 50MB를 초과하는 파일명 리스트
     */
    @JsonIgnore
    public List<String> getOversizedFiles() {
        return getValidFiles().stream()
                .filter(file -> file.getSize() > 50 * 1024 * 1024L)
                .map(MultipartFile::getOriginalFilename)
                .toList();
    }
}
