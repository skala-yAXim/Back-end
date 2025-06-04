package com.yaxim.project.service;

import com.yaxim.project.controller.dto.request.MultipartProjectCreateRequest;
import com.yaxim.project.controller.dto.request.MultipartProjectUpdateRequest;
import com.yaxim.project.controller.dto.response.ProjectPageResponse;
import com.yaxim.project.controller.dto.response.ProjectResponse;
import com.yaxim.project.entity.Project;
import com.yaxim.project.entity.ProjectFile;
import com.yaxim.project.exception.*;
import com.yaxim.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectFileService projectFileService;

    /**
     * 프로젝트 생성 (파일 포함/미포함 통합)
     */
    @Transactional
    public ProjectResponse createProject(MultipartProjectCreateRequest request) {
        // 팀장님 스타일: Service에서 직접 예외 검증 및 throw
        validateProjectBasicInfo(request);
        validateDateRange(request.getStartDate(), request.getEndDate());
        
        // 파일이 있는 경우에만 파일 검증
        if (request.hasFiles()) {
            validateFileUpload(request);
        }

        // 프로젝트 생성
        Project project = request.toEntity();
        Project savedProject = projectRepository.save(project);

        // 파일 업로드 처리 (파일이 있는 경우만)
        if (request.hasFiles()) {
            List<MultipartFile> validFiles = request.getValidFiles();
            try {
                List<ProjectFile> uploadedFiles = projectFileService.uploadProjectFiles(savedProject.getId(), validFiles);
                log.info("프로젝트 파일 업로드 성공 - 프로젝트 ID: {}, 업로드된 파일 수: {}", 
                        savedProject.getId(), uploadedFiles.size());
            } catch (Exception e) {
                log.warn("파일 업로드 실패 (프로젝트는 정상 생성됨) - 프로젝트 ID: {}, 에러: {}", 
                        savedProject.getId(), e.getMessage());
                // 파일 업로드 실패해도 프로젝트는 성공적으로 생성된 상태로 계속 진행
            }
        }

        String fileInfo = request.hasFiles() ? " (파일 " + request.getValidFiles().size() + "개 포함)" : " (파일 없음)";
        log.info("프로젝트 생성 완료{} - ID: {}, 프로젝트명: {}", fileInfo, savedProject.getId(), savedProject.getName());

        // 최신 상태로 다시 조회 (파일 정보 포함)
        Project refreshedProject = findProjectById(savedProject.getId());
        return ProjectResponse.from(refreshedProject);
    }

    /**
     * 프로젝트 단건 조회
     */
    public ProjectResponse getProject(Long projectId) {
        Project project = findProjectById(projectId);
        return ProjectResponse.from(project);
    }

    /**
     * 팀별 프로젝트 목록 조회 (페이징) - UI 설계서 기준 10개씩
     */
    public ProjectPageResponse getProjects(Long teamId, int page, int size, String sortBy, String sortDirection) {
        // UI 설계서: 페이징 당 프로젝트 10개씩 표시
        size = Math.min(size, 10);
        
        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<Project> projectPage = projectRepository.findByTeamIdOrderByIdDesc(teamId, pageable);

        Page<ProjectResponse> responsePage = projectPage.map(ProjectResponse::from);
        return ProjectPageResponse.from(responsePage);
    }

    /**
     * 프로젝트 수정 (파일 포함/미포함 통합)
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, MultipartProjectUpdateRequest request) {
        Project project = findProjectById(projectId);

        // 팀장님 스타일: Service에서 직접 예외 검증 및 throw
        validateProjectUpdateInfo(request);
        
        // 새 파일이 있는 경우에만 파일 검증
        if (request.hasNewFiles()) {
            validateFileUploadForUpdate(request);
        }

        // 기본 정보 수정
        updateProjectFields(project, request);

        // 파일 삭제 처리
        if (request.hasFilesToDelete()) {
            List<Long> deleteFileIds = request.getValidDeleteFileIds();
            for (Long fileId : deleteFileIds) {
                try {
                    projectFileService.deleteProjectFile(fileId);
                    log.info("파일 삭제 완료 - 파일 ID: {}", fileId);
                } catch (Exception e) {
                    log.warn("파일 삭제 실패 - 파일 ID: {}, 에러: {}", fileId, e.getMessage());
                }
            }
        }

        // 새 파일 업로드 처리
        if (request.hasNewFiles()) {
            List<MultipartFile> newFiles = request.getValidNewFiles();
            try {
                List<ProjectFile> uploadedFiles = projectFileService.uploadProjectFiles(projectId, newFiles);
                log.info("새 파일 업로드 성공 - 프로젝트 ID: {}, 업로드된 파일 수: {}", 
                        projectId, uploadedFiles.size());
            } catch (Exception e) {
                log.warn("새 파일 업로드 실패 (프로젝트 수정은 완료) - 프로젝트 ID: {}, 에러: {}", 
                        projectId, e.getMessage());
                // 파일 업로드 실패해도 프로젝트 수정은 완료된 상태로 계속 진행
            }
        }

        String updateInfo = buildUpdateLogInfo(request);
        log.info("프로젝트 수정 완료{} - ID: {}", updateInfo, projectId);

        // 최신 상태로 다시 조회 (파일 정보 포함)
        Project refreshedProject = findProjectById(projectId);
        return ProjectResponse.from(refreshedProject);
    }

    /**
     * 프로젝트 삭제
     */
    @Transactional
    public void deleteProject(Long projectId) {
        Project project = findProjectById(projectId);
        
        // 연관된 모든 파일 먼저 삭제
        try {
            projectFileService.deleteAllProjectFiles(projectId);
            log.info("프로젝트 파일 일괄 삭제 완료 - 프로젝트 ID: {}", projectId);
        } catch (Exception e) {
            log.warn("프로젝트 파일 삭제 중 오류 발생 - 프로젝트 ID: {}, 에러: {}", projectId, e.getMessage());
        }
        
        // 프로젝트 삭제
        projectRepository.delete(project);
        log.info("프로젝트 삭제 완료 - ID: {}", projectId);
    }

    // ========== 팀장님 스타일: Service에서 직접 검증 및 예외 throw ==========

    /**
     * 프로젝트 기본 정보 검증 (생성 시)
     */
    private void validateProjectBasicInfo(MultipartProjectCreateRequest request) {
        validateProjectName(request.getName());
        validateProjectDescription(request.getDescription());
    }

    /**
     * 프로젝트 수정 정보 검증
     */
    private void validateProjectUpdateInfo(MultipartProjectUpdateRequest request) {
        // 수정 시에는 null이 아닌 경우에만 검증
        if (request.getName() != null) {
            validateProjectName(request.getName());
        }
        if (request.getDescription() != null) {
            validateProjectDescription(request.getDescription());
        }
    }

    /**
     * 프로젝트명 검증
     */
    private void validateProjectName(String name) {
        if (name != null && name.length() > 100) {
            throw new ProjectNameTooLongException(name.length());
        }
    }

    /**
     * 프로젝트 설명 검증
     */
    private void validateProjectDescription(String description) {
        if (description != null && description.length() > 1000) {
            throw new ProjectDescriptionTooLongException(description.length());
        }
    }

    /**
     * 날짜 범위 검증 (팀장님 스타일로 변경)
     */
    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ProjectDateRangeInvalidException();
        }
    }

    /**
     * 파일 업로드 검증 (생성 시)
     */
    private void validateFileUpload(MultipartProjectCreateRequest request) {
        List<MultipartFile> validFiles = request.getValidFiles();
        
        validateFileCount(validFiles);
        validateFileSizes(validFiles);
        validateFileFormats(request.getInvalidFileFormats());
    }

    /**
     * 파일 업로드 검증 (수정 시)
     */
    private void validateFileUploadForUpdate(MultipartProjectUpdateRequest request) {
        List<MultipartFile> newFiles = request.getValidNewFiles();
        
        validateFileCount(newFiles);
        validateFileSizes(newFiles);
        validateFileFormatsForUpdate(newFiles);
    }

    /**
     * 파일 개수 검증 (최대 5개)
     */
    private void validateFileCount(List<MultipartFile> files) {
        if (files.size() > 5) {
            throw new FileCountExceededException(files.size());
        }
    }

    /**
     * 파일 크기 검증 (각 파일 최대 50MB)
     */
    private void validateFileSizes(List<MultipartFile> files) {
        final long MAX_FILE_SIZE = 50 * 1024 * 1024L; // 50MB
        
        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                String formattedSize = formatFileSize(file.getSize());
                throw new FileSizeExceededException(file.getOriginalFilename(), formattedSize);
            }
        }
    }

    /**
     * 파일 형식 검증 (생성 시)
     */
    private void validateFileFormats(List<String> invalidFiles) {
        if (!invalidFiles.isEmpty()) {
            throw new FileFormatNotSupportedException(String.join(", ", invalidFiles));
        }
    }

    /**
     * 파일 형식 검증 (수정 시)
     */
    private void validateFileFormatsForUpdate(List<MultipartFile> files) {
        List<String> invalidFiles = files.stream()
                .filter(file -> !isAllowedFileExtension(file.getOriginalFilename()))
                .map(MultipartFile::getOriginalFilename)
                .toList();
                
        if (!invalidFiles.isEmpty()) {
            throw new FileFormatNotSupportedException(String.join(", ", invalidFiles));
        }
    }

    /**
     * 허용된 파일 확장자 검증
     */
    private boolean isAllowedFileExtension(String filename) {
        if (filename == null) return false;
        String extension = filename.toLowerCase();
        String[] allowedExtensions = {".docx", ".xlsx", ".txt"};
        
        for (String allowed : allowedExtensions) {
            if (extension.endsWith(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 파일 크기를 읽기 쉬운 형태로 포맷
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + "B";
        if (size < 1024 * 1024) return String.format("%.1fKB", size / 1024.0);
        return String.format("%.1fMB", size / (1024.0 * 1024.0));
    }

    /**
     * 수정 로그 정보 생성
     */
    private String buildUpdateLogInfo(MultipartProjectUpdateRequest request) {
        StringBuilder info = new StringBuilder();
        
        if (request.hasNewFiles()) {
            info.append(" (새 파일 ").append(request.getValidNewFiles().size()).append("개 추가)");
        }
        if (request.hasFilesToDelete()) {
            info.append(" (파일 ").append(request.getValidDeleteFileIds().size()).append("개 삭제)");
        }
        if (info.length() == 0) {
            info.append(" (파일 변경 없음)");
        }
        
        return info.toString();
    }

    // === Private Helper Methods ===

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private void updateProjectFields(Project project, MultipartProjectUpdateRequest request) {
        // 날짜 유효성 검증
        LocalDateTime startDate = request.getStartDate() != null ? request.getStartDate() : project.getStartDate();
        LocalDateTime endDate = request.getEndDate() != null ? request.getEndDate() : project.getEndDate();
        validateDateRange(startDate, endDate);

        // 기본 정보 업데이트
        if (request.hasNameUpdate() || request.hasDescriptionUpdate()) {
            project.updateBasicInfo(request.getName(), request.getDescription());
        }

        // 날짜 업데이트
        if (request.hasDateUpdate()) {
            project.updateDates(request.getStartDate(), request.getEndDate());
        }

        // 팀 업데이트
        if (request.hasTeamIdUpdate()) {
            project.updateTeam(request.getTeamId());
        }
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        // 페이지 크기 제한 (UI 설계서: 최대 10개)
        size = Math.min(size, 10);
        
        // 정렬 방향 설정
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        // 정렬 필드 설정 (기본값: id)
        String sortField = getSortField(sortBy);
        Sort sort = Sort.by(direction, sortField);

        return PageRequest.of(page, size, sort);
    }

    private String getSortField(String sortBy) {
        return switch (sortBy) {
            case "name", "projectName" -> "name"; // projectName도 name으로 매핑
            case "startDate" -> "startDate";
            case "endDate" -> "endDate";
            case "teamId" -> "teamId";
            // status는 동적 계산되므로 정렬 불가, id로 대체
            case "status" -> "id";
            default -> "id";
        };
    }
}
