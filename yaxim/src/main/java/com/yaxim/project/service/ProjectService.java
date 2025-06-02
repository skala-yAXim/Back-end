package com.yaxim.project.service;

import com.yaxim.project.controller.dto.request.MultipartProjectCreateRequest;
import com.yaxim.project.controller.dto.request.MultipartProjectUpdateRequest;
import com.yaxim.project.controller.dto.request.ProjectCreateRequest;
import com.yaxim.project.controller.dto.request.ProjectUpdateRequest;
import com.yaxim.project.controller.dto.response.ProjectPageResponse;
import com.yaxim.project.controller.dto.response.ProjectResponse;
import com.yaxim.project.entity.Project;
import com.yaxim.project.entity.ProjectFile;
import com.yaxim.project.entity.exception.ProjectNotFoundException;
import com.yaxim.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectFileService projectFileService;

    /**
     * 프로젝트 생성
     */
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        // 날짜 유효성 검증
        validateDateRange(request.getStartDate(), request.getEndDate());

        Project project = request.toEntity();
        Project savedProject = projectRepository.save(project);

        log.info("프로젝트 생성 완료 - ID: {}, 프로젝트명: {}", savedProject.getId(), savedProject.getName());

        return ProjectResponse.from(savedProject);
    }

    /**
     * 프로젝트 생성 (파일 포함)
     */
    @Transactional
    public ProjectResponse createProjectWithFiles(MultipartProjectCreateRequest request) {
        // 날짜 유효성 검증
        validateDateRange(request.getStartDate(), request.getEndDate());

        // 프로젝트 생성
        Project project = request.toEntity();
        Project savedProject = projectRepository.save(project);

        // 파일 업로드 처리
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

        log.info("프로젝트 생성 완료 (파일 포함) - ID: {}, 프로젝트명: {}", savedProject.getId(), savedProject.getName());

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
     * 프로젝트 수정
     */
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        Project project = findProjectById(projectId);

        // 날짜 유효성 검증
        LocalDateTime startDate = request.hasStartDate() ? request.getStartDate() : project.getStartDate();
        LocalDateTime endDate = request.hasEndDate() ? request.getEndDate() : project.getEndDate();
        validateDateRange(startDate, endDate);

        // 프로젝트 정보 업데이트
        updateProjectFields(project, request);

        log.info("프로젝트 수정 완료 - ID: {}", projectId);

        return ProjectResponse.from(project);
    }

    /**
     * 프로젝트 전체 수정 (파일 포함)
     */
    @Transactional
    public ProjectResponse updateProjectWithFiles(Long projectId, MultipartProjectUpdateRequest request) {
        Project project = findProjectById(projectId);

        // 기본 정보 수정
        updateProjectFieldsFromMultipartRequest(project, request);

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

        log.info("프로젝트 전체 수정 완료 - ID: {}", projectId);

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

    // === Private Helper Methods ===

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 이후여야 합니다.");
        }
    }

    private void updateProjectFields(Project project, ProjectUpdateRequest request) {
        if (request.hasName() || request.hasDescription()) {
            project.updateBasicInfo(request.getName(), request.getDescription());
        }

        if (request.hasStartDate() || request.hasEndDate()) {
            project.updateDates(request.getStartDate(), request.getEndDate());
        }

        if (request.hasTeamId()) {
            project.updateTeam(request.getTeamId());
        }
    }

    private void updateProjectFieldsFromMultipartRequest(Project project, MultipartProjectUpdateRequest request) {
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
