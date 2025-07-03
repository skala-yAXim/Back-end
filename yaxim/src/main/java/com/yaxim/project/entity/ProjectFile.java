package com.yaxim.project.entity;

import com.yaxim.global.util.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_files")
public class ProjectFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ projectId를 읽기 전용으로 설정 (JPA 관계에서 자동 관리)
    @Column(name = "project_id", insertable = false, updatable = false)
    private Long projectId;

    @NotNull
    @Size(min = 1, max = 255, message = "원본 파일명은 1자 이상 255자 이하여야 합니다.")
    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @NotNull
    @Size(min = 1, max = 255, message = "저장된 파일명은 1자 이상 255자 이하여야 합니다.")
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @NotNull
    @Size(min = 1, max = 500, message = "파일 URL은 1자 이상 500자 이하여야 합니다.")
    @Column(name = "file_url", nullable = false, length = 500)
    private String fileUrl;

    @NotNull
    @Size(min = 1, max = 100, message = "파일 타입은 1자 이상 100자 이하여야 합니다.")
    @Column(name = "file_type", nullable = false, length = 100)
    private String fileType;

    @NotNull
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "s3_bucket_name", length = 100)
    private String s3BucketName;

    @Column(name = "s3_object_key", length = 500)
    private String s3ObjectKey;

    // ✅ Project와의 양방향 관계 설정 (Project가 관계 주인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    // 비즈니스 메서드들
    public void updateFileInfo(String originalFileName, String storedFileName, String fileUrl, 
                              String fileType, Long fileSize) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public void updateS3Info(String s3BucketName, String s3ObjectKey) {
        this.s3BucketName = s3BucketName;
        this.s3ObjectKey = s3ObjectKey;
    }

    // ✅ Project 설정 시 projectId 자동 동기화
    public void setProject(Project project) {
        this.project = project;
        this.projectId = project != null ? project.getId() : null;
    }

    // 파일 확장자 추출
    public String getFileExtension() {
        if (originalFileName == null) return "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        return lastDotIndex > 0 ? originalFileName.substring(lastDotIndex + 1) : "";
    }

    // 파일 크기 표시용 (KB, MB 단위)
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        double size = fileSize.doubleValue();
        if (size >= 1024 * 1024) {
            return String.format("%.1f MB", size / (1024 * 1024));
        } else if (size >= 1024) {
            return String.format("%.1f KB", size / 1024);
        } else {
            return size + " B";
        }
    }

    // 이미지 파일 여부 확인
    public boolean isImageFile() {
        String extension = getFileExtension().toLowerCase();
        return extension.equals("jpg") || extension.equals("jpeg") || 
               extension.equals("png") || extension.equals("gif") || 
               extension.equals("bmp") || extension.equals("webp");
    }

    // PDF 파일 여부 확인
    public boolean isPdfFile() {
        return getFileExtension().toLowerCase().equals("pdf");
    }

    // Office 파일 여부 확인
    public boolean isOfficeFile() {
        String extension = getFileExtension().toLowerCase();
        return extension.equals("doc") || extension.equals("docx") || 
               extension.equals("xls") || extension.equals("xlsx") || 
               extension.equals("ppt") || extension.equals("pptx");
    }
}
