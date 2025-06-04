package com.yaxim.project.service;

import com.yaxim.project.controller.dto.request.ProjectCreateRequest;
import com.yaxim.project.controller.dto.request.ProjectUpdateRequest;
import com.yaxim.project.controller.dto.response.ProjectDetailResponse;
import com.yaxim.project.controller.dto.response.ProjectResponse;
import com.yaxim.project.entity.Project;
import com.yaxim.project.exception.*;
import com.yaxim.project.repository.ProjectRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserHasNoAuthorityException;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    /**
     * 프로젝트 생성 (파일 포함/미포함 통합)
     */
    @Transactional
    public ProjectDetailResponse createProject(
            ProjectCreateRequest request,
            Long userId
    ) {
        // 팀장님 스타일: Service에서 직접 예외 검증 및 throw
        validateProjectInfo(request);
        validateDateRange(request.getStartDate(), request.getEndDate());

        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Team team = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam();

        // 프로젝트 생성
        Project project = Project.builder()
                .name(request.getName())
                .team(team)
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        List<MultipartFile> files = request.getFiles();

        // 파일 업로드 처리 (파일이 있는 경우만)
        if (!files.isEmpty()) {
            List<MultipartFile> validFiles = validateFileUpload(files);
            projectFileService.uploadProjectFiles(project, validFiles);
        }

        return ProjectDetailResponse.from(projectRepository.save(project));
    }

    /**
     * 프로젝트 단건 조회
     */
    public ProjectDetailResponse getProject(Long projectId) {
        Project project = findProjectById(projectId);
        return ProjectDetailResponse.from(project);
    }

    /**
     * 팀별 프로젝트 목록 조회 (페이징) - UI 설계서 기준 10개씩
     */
    public List<ProjectResponse> getProjects(Pageable pageable, Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Team team = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam();

        Page<Project> projectPage = projectRepository.findByTeam(team, pageable);

        return getProjectResponseList(projectPage);
    }

    private List<ProjectResponse> getProjectResponseList(Page<Project> projectList) {
        return projectList.stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getStartDate(),
                        p.getEndDate(),
                        p.calculateStatus()
                ))
                .toList();
    }

    /**
     * 프로젝트 수정 (파일 포함/미포함 통합)
     */
    @Transactional
    public ProjectDetailResponse updateProject(ProjectUpdateRequest request, Long userId) {

        // 팀장님 스타일: Service에서 직접 예외 검증 및 throw
        validateProjectInfo(request);
        
        // 새 파일이 있는 경우에만 파일 검증
        if (!request.getFiles().isEmpty()) {
            validateFileUpload(request.getFiles());
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Team team = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam();

        Project project = projectRepository.findById(request.getId())
                .orElseThrow(ProjectNotFoundException::new);

        if (project.getTeam() != team) {
            throw new UserHasNoAuthorityException();
        }

        // 기본 정보 수정
        updateProjectFields(project, request);

        // 파일 삭제 처리
        if (!request.getDeleteFileIds().isEmpty()) {
            List<Long> deleteFileIds = request.getDeleteFileIds();
            for (Long fileId : deleteFileIds) {
                try {
                    projectFileService.deleteProjectFile(fileId);
                } catch (Exception e) {
                    throw new FileDeleteFailedException();
                }
            }
        }

        // 새 파일 업로드 처리
        if (!request.getFiles().isEmpty()) {
            List<MultipartFile> newFiles = request.getFiles();
            validateFileUpload(newFiles);
            try {
                projectFileService.uploadProjectFiles(project, newFiles);
            } catch (Exception e) {
                throw new FileUploadFailedException();
            }
        }

        return ProjectDetailResponse.from(projectRepository.save(project));
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
        } catch (Exception e) {
            throw new FileDeleteFailedException();
        }
        
        // 프로젝트 삭제
        projectRepository.delete(project);
    }

    // ========== 팀장님 스타일: Service에서 직접 검증 및 예외 throw ==========

    /**
     * 프로젝트 기본 정보 검증 (생성 시)
     */
    private void validateProjectInfo(ProjectCreateRequest request) {
        validateProjectName(request.getName());
        validateProjectDescription(request.getDescription());
    }

    private void validateProjectInfo(ProjectUpdateRequest request) {
        validateProjectName(request.getName());
        validateProjectDescription(request.getDescription());
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
    private List<MultipartFile> validateFileUpload(List<MultipartFile> files) {
        List<MultipartFile> validFiles = getValidFiles(files);
        
        validateFileCount(validFiles);
        validateFileSizes(validFiles);
        validateFileFormats(getInvalidFileFormats(validFiles));

        return validFiles;
    }

    public List<MultipartFile> getValidFiles(List<MultipartFile> files) {
        if (files == null) return List.of();
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .toList();
    }

    public List<String> getInvalidFileFormats(List<MultipartFile> files) {
        return files.stream()
                .map(MultipartFile::getOriginalFilename)
                .filter(originalFilename -> {
                    if (originalFilename == null) return true;
                    String extension = originalFilename.toLowerCase();
                    return !extension.endsWith(".docx") &&
                            !extension.endsWith(".xlsx") &&
                            !extension.endsWith(".txt");
                })
                .toList();
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
                throw new FileSizeExceededException();
            }
        }
    }

    /**
     * 파일 형식 검증 (생성 시)
     */
    private void validateFileFormats(List<String> invalidFiles) {
        if (!invalidFiles.isEmpty()) {
            throw new FileFormatNotSupportedException();
        }
    }

    /**
     * 파일 크기를 읽기 쉬운 형태로 포맷
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + "B";
        if (size < 1024 * 1024) return String.format("%.1fKB", size / 1024.0);
        return String.format("%.1fMB", size / (1024.0 * 1024.0));
    }

    // === Private Helper Methods ===

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    private void updateProjectFields(Project project, ProjectUpdateRequest request) {
        // 기본 정보 업데이트
        if (!request.getName().isEmpty()) {
            project.setName(request.getName());
        }
        if (!request.getDescription().isEmpty()) {
            project.setDescription(request.getDescription());
        }

        // 날짜 업데이트
        if (request.getStartDate() != null || request.getEndDate() != null) {
            validateDateRange(request.getStartDate(), request.getEndDate());
            project.updateDates(request.getStartDate(), request.getEndDate());
        }
    }
}
