package com.yaxim.project.controller;

import com.yaxim.project.controller.dto.request.MultipartProjectCreateRequest;
import com.yaxim.project.controller.dto.request.MultipartProjectUpdateRequest;
import com.yaxim.project.controller.dto.request.ProjectCreateRequest;
import com.yaxim.project.controller.dto.request.ProjectUpdateRequest;
import com.yaxim.project.controller.dto.response.ProjectPageResponse;
import com.yaxim.project.controller.dto.response.ProjectResponse;
import com.yaxim.project.service.ProjectService;
import com.yaxim.project.service.validator.ProjectValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 프로젝트 관리 컨트롤러
 */
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "프로젝트 관리", description = "프로젝트 CRUD 및 파일 관리")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectValidator projectValidator;

    // ========== 생성 ==========

    @PostMapping
    @Operation(summary = "프로젝트 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request) {
        
        log.info("프로젝트 생성 - {}", request.getName());
        projectValidator.validateProjectBasicInfo(request);
        
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로젝트 생성 (파일 포함)")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @ApiResponse(responseCode = "413", description = "파일 크기 초과")
    public ResponseEntity<ProjectResponse> createProjectWithFiles(
            @Parameter(description = "프로젝트명", required = true)
            @RequestParam("name") String name,
            
            @Parameter(description = "팀 ID", required = true)
            @RequestParam("teamId") Long teamId,
            
            @Parameter(description = "시작일 (ISO 8601)", required = true, example = "2025-01-01T09:00:00")
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            
            @Parameter(description = "종료일 (ISO 8601)", required = true, example = "2025-12-31T18:00:00")
            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            
            @Parameter(description = "설명")
            @RequestParam(value = "description", required = false) String description,
            
            @Parameter(description = "파일들")
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        
        log.info("프로젝트 생성 (파일 포함) - {}", name);
        
        MultipartProjectCreateRequest request = MultipartProjectCreateRequest.builder()
                .name(name).teamId(teamId).startDate(startDate).endDate(endDate)
                .description(description).files(files).build();
        
        projectValidator.validateProjectBasicInfo(request);
        projectValidator.validateFileUpload(request);
        logFileUploadInfo(request);
        
        ProjectResponse response = projectService.createProjectWithFiles(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== 조회 ==========

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 상세 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "프로젝트 없음")
    public ResponseEntity<ProjectResponse> getProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        ProjectResponse response = projectService.getProject(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "프로젝트 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<ProjectPageResponse> getProjects(
            @Parameter(description = "팀 ID", required = true) @RequestParam Long teamId,
            @Parameter(description = "페이지 (0부터)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "정렬 기준", example = "id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "정렬 방향", example = "desc") @RequestParam(defaultValue = "desc") String sortDirection) {
        
        ProjectPageResponse response = projectService.getProjects(teamId, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    // ========== 수정 ==========

    @PatchMapping("/{projectId}")
    @Operation(summary = "프로젝트 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "프로젝트 없음")
    public ResponseEntity<ProjectResponse> updateProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        
        log.info("프로젝트 수정 - ID: {}", projectId);
        ProjectResponse response = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{projectId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로젝트 수정 (파일 포함)")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "프로젝트 없음")
    public ResponseEntity<ProjectResponse> updateProjectWithFiles(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId,
            @Parameter(description = "프로젝트명") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "팀 ID") @RequestParam(value = "teamId", required = false) Long teamId,
            @Parameter(description = "시작일") @RequestParam(value = "startDate", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료일") @RequestParam(value = "endDate", required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "설명") @RequestParam(value = "description", required = false) String description,
            @Parameter(description = "새 파일들") @RequestParam(value = "newFiles", required = false) List<MultipartFile> newFiles,
            @Parameter(description = "삭제할 파일 ID들") @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds) {
        
        log.info("프로젝트 수정 (파일 포함) - ID: {}", projectId);
        
        MultipartProjectUpdateRequest request = MultipartProjectUpdateRequest.builder()
                .name(name).teamId(teamId).startDate(startDate).endDate(endDate)
                .description(description).newFiles(newFiles).deleteFileIds(deleteFileIds).build();
        
        ProjectResponse response = projectService.updateProjectWithFiles(projectId, request);
        return ResponseEntity.ok(response);
    }

    // ========== 삭제 ==========

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 삭제")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "프로젝트 없음")
    public ResponseEntity<Void> deleteProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        log.info("프로젝트 삭제 - ID: {}", projectId);
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    // ========== 유틸리티 ==========

    private void logFileUploadInfo(MultipartProjectCreateRequest request) {
        if (request.hasFiles()) {
            int count = request.getValidFiles().size();
            long totalMB = request.getValidFiles().stream()
                    .mapToLong(MultipartFile::getSize).sum() / (1024 * 1024);
            log.info("파일 업로드 - {}개, {}MB", count, totalMB);
        }
    }
}
