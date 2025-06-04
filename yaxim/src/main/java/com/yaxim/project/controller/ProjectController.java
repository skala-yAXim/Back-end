package com.yaxim.project.controller;

import com.yaxim.global.auth.aop.CheckRole;
import com.yaxim.global.auth.jwt.JwtAuthentication;
import com.yaxim.project.controller.dto.request.ProjectCreateRequest;
import com.yaxim.project.controller.dto.request.ProjectUpdateRequest;
import com.yaxim.project.controller.dto.response.ProjectDetailResponse;
import com.yaxim.project.controller.dto.response.ProjectResponse;
import com.yaxim.project.service.ProjectService;
import com.yaxim.user.entity.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 프로젝트 관리 컨트롤러 (통합 버전)
 * - 파일 포함/미포함을 하나의 엔드포인트로 통합
 * - 팀장님 스타일 예외처리 적용
 */
@CheckRole(UserRole.LEADER)
@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "프로젝트 관리", description = "프로젝트 CRUD 및 파일 관리 (통합)")
public class ProjectController {

    private final ProjectService projectService;

    // ========== 생성 ==========

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로젝트 생성")
    @ApiResponse(responseCode = "201", description = "생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청")
    @ApiResponse(responseCode = "413", description = "파일 크기 초과")
    @ApiResponse(responseCode = "415", description = "미지원 파일 형식")
    public ResponseEntity<ProjectDetailResponse> createProject(
            @ModelAttribute @Validated ProjectCreateRequest request,
            JwtAuthentication auth
    ) {
        ProjectDetailResponse response = projectService.createProject(request, auth.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== 조회 ==========

    @GetMapping
    @Operation(summary = "프로젝트 목록 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            Pageable pageable,
            JwtAuthentication auth
    ) {
        return ResponseEntity.ok(
                projectService.getProjects(
                        pageable,
                        auth.getUserId()
                )
        );
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 상세 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "프로젝트 없음")
    public ResponseEntity<ProjectDetailResponse> getProject(
            @Parameter(description = "프로젝트 ID") @PathVariable Long projectId) {
        ProjectDetailResponse response = projectService.getProject(projectId);
        return ResponseEntity.ok(response);
    }

    // ========== 수정 ==========

    @PatchMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로젝트 수정")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "프로젝트 없음")
    @ApiResponse(responseCode = "413", description = "파일 크기 초과")
    @ApiResponse(responseCode = "415", description = "미지원 파일 형식")
    public ResponseEntity<ProjectDetailResponse> updateProject(
            @ModelAttribute @Validated ProjectUpdateRequest request,
            JwtAuthentication auth
    ) {

        ProjectDetailResponse response = projectService.updateProject(request, auth.getUserId());
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
}
