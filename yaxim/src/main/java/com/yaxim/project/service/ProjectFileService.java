package com.yaxim.project.service;

import com.yaxim.global.s3.S3Service;
import com.yaxim.project.entity.Project;
import com.yaxim.project.entity.ProjectFile;
import com.yaxim.project.exception.ProjectFileNotFoundException;
import com.yaxim.project.repository.ProjectFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectFileService {

    private final ProjectFileRepository projectFileRepository;
    private final S3Service s3Service;

    /**
     * 파일 업로드 (일부 실패해도 성공한 파일은 유지)
     */
    @Transactional
    public void uploadProjectFiles(Project project, List<MultipartFile> files) {
        
        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            ProjectFile uploadedFile = uploadSingleProjectFile(project, file);
            project.addProjectFile(uploadedFile);
        }
    }
    
    /**
     * 개별 파일 업로드
     */
    private ProjectFile uploadSingleProjectFile(Project project, MultipartFile file) {

        // 중복 파일명 처리 (자동 리네임)
        String originalFileName = file.getOriginalFilename();
        Optional<ProjectFile> existingFile = projectFileRepository
                .findByProjectIdAndOriginalFileName(project.getId(), originalFileName);
        
        if (existingFile.isPresent()) {
            // 자동 리네임
            String timestamp = String.valueOf(System.currentTimeMillis());
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                int lastDot = originalFileName.lastIndexOf(".");
                extension = originalFileName.substring(lastDot);
                originalFileName = originalFileName.substring(0, lastDot) + "_" + timestamp + extension;
            } else {
                originalFileName = originalFileName + "_" + timestamp;
            }
        }

        S3Service.S3UploadResult uploadResult = s3Service.uploadProjectFile(
                file,
                project.getId()
        );

        return ProjectFile.builder()
                .originalFileName(originalFileName)
                .storedFileName(uploadResult.getStoredFileName())
                .fileUrl(uploadResult.getFileUrl())
                .fileType(uploadResult.getFileType())
                .fileSize(uploadResult.getFileSize())
                .s3BucketName(uploadResult.getBucketName())
                .s3ObjectKey(uploadResult.getS3ObjectKey())
                .build();
    }

    /**
     * 프로젝트 파일 삭제
     */
    @Transactional
    public void deleteProjectFile(Long fileId) {
        ProjectFile projectFile = findProjectFileById(fileId);

        // ✅ 양방향 관계 해제
        if (projectFile.getProject() != null) {
            projectFile.getProject().removeProjectFile(projectFile);
        }

        // S3에서 파일 삭제
        s3Service.deleteFile(projectFile.getS3ObjectKey());

        // DB에서 파일 정보 삭제
        projectFileRepository.delete(projectFile);
    }

    /**
     * 프로젝트의 모든 파일 삭제
     */
    @Transactional
    public void deleteAllProjectFiles(Long projectId) {
        List<ProjectFile> projectFiles = projectFileRepository.findByProjectIdOrderByCreatedAtDesc(projectId);

        // S3에서 모든 파일 삭제
        s3Service.deleteProjectFiles(projectId);

        // DB에서 모든 파일 정보 삭제
        projectFileRepository.deleteByProjectId(projectId);
    }

    // === Private Helper Methods ===

    private ProjectFile findProjectFileById(Long fileId) {
        return projectFileRepository.findById(fileId)
                .orElseThrow(() -> new ProjectFileNotFoundException(fileId));
    }

    /**
     * 프로젝트 파일 통계 DTO
     */
    @lombok.Builder
    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class ProjectFileStats {
        private long totalFileCount;
        private Long totalFileSize;
        private long imageFileCount;
        private long pdfFileCount;
        private long officeFileCount;

        public String getFormattedTotalSize() {
            if (totalFileSize == null || totalFileSize == 0) {
                return "0 B";
            }
            
            double size = totalFileSize.doubleValue();
            if (size >= 1024 * 1024 * 1024) {
                return String.format("%.2f GB", size / (1024 * 1024 * 1024));
            } else if (size >= 1024 * 1024) {
                return String.format("%.2f MB", size / (1024 * 1024));
            } else if (size >= 1024) {
                return String.format("%.2f KB", size / 1024);
            } else {
                return size + " B";
            }
        }
    }
}
