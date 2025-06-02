package com.yaxim.project.service;

import com.yaxim.project.entity.Project;
import com.yaxim.project.entity.ProjectFile;
import com.yaxim.project.entity.exception.ProjectFileNotFoundException;
import com.yaxim.project.entity.exception.ProjectNotFoundException;
import com.yaxim.project.repository.ProjectFileRepository;
import com.yaxim.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectFileService {

    private final ProjectFileRepository projectFileRepository;
    private final ProjectRepository projectRepository;
    private final S3Service s3Service;

    /**
     * 프로젝트 파일 업로드 (단일 파일)
     */
    @Transactional
    public ProjectFile uploadProjectFile(Long projectId, MultipartFile file) {
        return uploadSingleProjectFile(projectId, file);
    }

    /**
     * ✅ 안전한 여러 파일 일괄 업로드 (일부 실패해도 성공한 파일은 유지)
     */
    @Transactional
    public List<ProjectFile> uploadProjectFiles(Long projectId, List<MultipartFile> files) {
        List<ProjectFile> successfulUploads = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        
        log.info("=== ProjectFileService.uploadProjectFiles 시작 ===");
        log.info("프로젝트 ID: {}", projectId);
        log.info("전달받은 파일 수: {}", files != null ? files.size() : 0);
        
        if (files == null || files.isEmpty()) {
            log.warn("파일 리스트가 null이거나 비어있습니다.");
            return successfulUploads;
        }
        
        // ✅ 파일별 상세 정보 로깅
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            log.info("파일 [{}]: name={}, originalFilename={}, size={}, isEmpty={}", 
                    i, 
                    file != null ? file.getName() : "null",
                    file != null ? file.getOriginalFilename() : "null",
                    file != null ? file.getSize() : 0,
                    file != null ? file.isEmpty() : true);
        }
        
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            try {
                log.info("파일 [{}] 업로드 시작: {}", i, file.getOriginalFilename());
                
                // ✅ 파일별로 개별 처리 (다른 파일에 영향 안 주기 위해)
                ProjectFile uploadedFile = uploadSingleProjectFile(projectId, file);
                successfulUploads.add(uploadedFile);
                
                log.info("파일 [{}] 업로드 성공: {} (파일 ID: {})", 
                        i, file.getOriginalFilename(), uploadedFile.getId());
                
            } catch (Exception e) {
                failedFiles.add(file.getOriginalFilename());
                log.error("파일 [{}] 업로드 실패: {}, 에러: {}", 
                        i, file.getOriginalFilename(), e.getMessage(), e);
            }
        }
        
        log.info("=== ProjectFileService.uploadProjectFiles 완료 ===");
        log.info("성공한 파일 수: {}", successfulUploads.size());
        log.info("실패한 파일 수: {}", failedFiles.size());
        
        if (!failedFiles.isEmpty()) {
            log.warn("실패한 파일들: {}", String.join(", ", failedFiles));
        }
        
        if (!successfulUploads.isEmpty()) {
            List<String> successFileNames = successfulUploads.stream()
                    .map(ProjectFile::getOriginalFileName)
                    .toList();
            log.info("성공한 파일들: {}", String.join(", ", successFileNames));
        }
        
        return successfulUploads;
    }
    
    /**
     * ✅ 개별 파일 업로드 (다른 파일에 영향 주지 않도록 분리)
     */
    private ProjectFile uploadSingleProjectFile(Long projectId, MultipartFile file) {
        log.info("uploadSingleProjectFile 시작 - 프로젝트 ID: {}, 파일: {}", 
                projectId, file.getOriginalFilename());
        
        // ✅ Project 엔티티 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
                
        // ✅ 중복 파일명 처리 (자동 리네임)
        String originalFileName = file.getOriginalFilename();
        Optional<ProjectFile> existingFile = projectFileRepository
                .findByProjectIdAndOriginalFileName(projectId, originalFileName);
        
        if (existingFile.isPresent()) {
            // ✅ 중복 파일명 자동 리네임
            String timestamp = String.valueOf(System.currentTimeMillis());
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                int lastDot = originalFileName.lastIndexOf(".");
                extension = originalFileName.substring(lastDot);
                originalFileName = originalFileName.substring(0, lastDot) + "_" + timestamp + extension;
            } else {
                originalFileName = originalFileName + "_" + timestamp;
            }
            log.info("중복 파일명 자동 리네임: {} -> {}", file.getOriginalFilename(), originalFileName);
        }

        // ✅ S3에 파일 업로드
        log.info("S3 업로드 시작: {}", originalFileName);
        S3Service.S3UploadResult uploadResult = s3Service.uploadProjectFile(file, projectId);
        log.info("S3 업로드 완료: {} -> {}", originalFileName, uploadResult.getS3ObjectKey());

        // ✅ DB에 파일 정보 저장 (리네임된 파일명 사용)
        ProjectFile projectFile = ProjectFile.builder()
                .originalFileName(originalFileName)  // ✅ 리네임된 파일명 사용
                .storedFileName(uploadResult.getStoredFileName())
                .fileUrl(uploadResult.getFileUrl())
                .fileType(uploadResult.getFileType())
                .fileSize(uploadResult.getFileSize())
                .s3BucketName(uploadResult.getBucketName())
                .s3ObjectKey(uploadResult.getS3ObjectKey())
                .build();

        // ✅ 양방향 관계 설정
        projectFile.setProject(project);
        project.addProjectFile(projectFile);

        ProjectFile savedFile = projectFileRepository.save(projectFile);
        
        log.info("DB 저장 완료 - 파일 ID: {}, 파일명: {}", 
                savedFile.getId(), savedFile.getOriginalFileName());

        return savedFile;
    }

    /**
     * 프로젝트 파일 목록 조회
     */
    public List<ProjectFile> getProjectFiles(Long projectId) {
        return projectFileRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    /**
     * 파일 다운로드 URL 생성
     */
    public String generateDownloadUrl(Long fileId) {
        ProjectFile projectFile = findProjectFileById(fileId);
        return s3Service.generatePresignedUrl(projectFile.getS3ObjectKey());
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

        log.info("프로젝트 파일 삭제 완료 - 파일 ID: {}, 파일명: {}", 
                fileId, projectFile.getOriginalFileName());
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

        log.info("프로젝트 전체 파일 삭제 완료 - 프로젝트 ID: {}, 삭제된 파일 수: {}", 
                projectId, projectFiles.size());
    }

    /**
     * 파일별 타입 필터링 조회
     */
    public List<ProjectFile> getProjectFilesByType(Long projectId, String fileType) {
        if ("image".equalsIgnoreCase(fileType)) {
            return projectFileRepository.findImageFilesByProjectId(projectId);
        } else if ("pdf".equalsIgnoreCase(fileType)) {
            return projectFileRepository.findPdfFilesByProjectId(projectId);
        } else if ("office".equalsIgnoreCase(fileType)) {
            return projectFileRepository.findOfficeFilesByProjectId(projectId);
        } else {
            return projectFileRepository.findByProjectIdAndFileTypeContainingOrderByCreatedAtDesc(projectId, fileType);
        }
    }

    /**
     * 프로젝트 파일 통계 조회
     */
    public ProjectFileStats getProjectFileStats(Long projectId) {
        long fileCount = projectFileRepository.countByProjectId(projectId);
        Long totalSize = projectFileRepository.sumFileSizeByProjectId(projectId).orElse(0L);
        
        List<ProjectFile> imageFiles = projectFileRepository.findImageFilesByProjectId(projectId);
        List<ProjectFile> pdfFiles = projectFileRepository.findPdfFilesByProjectId(projectId);
        List<ProjectFile> officeFiles = projectFileRepository.findOfficeFilesByProjectId(projectId);

        return ProjectFileStats.builder()
                .totalFileCount(fileCount)
                .totalFileSize(totalSize)
                .imageFileCount(imageFiles.size())
                .pdfFileCount(pdfFiles.size())
                .officeFileCount(officeFiles.size())
                .build();
    }

    /**
     * 파일 단건 조회
     */
    public ProjectFile getProjectFile(Long fileId) {
        return findProjectFileById(fileId);
    }

    /**
     * 파일 정보 업데이트 (파일명 변경 등)
     */
    @Transactional
    public ProjectFile updateProjectFile(Long fileId, String newOriginalFileName) {
        ProjectFile projectFile = findProjectFileById(fileId);
        
        // 원본 파일명만 변경 (실제 파일은 그대로)
        projectFile.updateFileInfo(
                newOriginalFileName, 
                projectFile.getStoredFileName(),
                projectFile.getFileUrl(),
                projectFile.getFileType(),
                projectFile.getFileSize()
        );

        log.info("프로젝트 파일 정보 업데이트 완료 - 파일 ID: {}, 새 파일명: {}", 
                fileId, newOriginalFileName);

        return projectFile;
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
