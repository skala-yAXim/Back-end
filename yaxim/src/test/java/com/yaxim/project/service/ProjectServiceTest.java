package com.yaxim.project.service;

import com.yaxim.project.controller.dto.request.ProjectCreateRequest;
import com.yaxim.project.controller.dto.request.ProjectSearchRequest;
import com.yaxim.project.controller.dto.request.ProjectUpdateRequest;
import com.yaxim.project.controller.dto.response.ProjectDetailResponse;
import com.yaxim.project.controller.dto.response.ProjectResponse;
import com.yaxim.project.entity.Project;
import com.yaxim.project.exception.*;
import com.yaxim.project.repository.ProjectCustomRepository;
import com.yaxim.project.repository.ProjectRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserHasNoAuthorityException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectFileService projectFileService;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private ProjectCustomRepository projectCustomRepository;

    @InjectMocks
    private ProjectService projectService;

    // 테스트 데이터 상수
    private static final Long USER_ID = 1L;
    private static final Long PROJECT_ID = 100L;
    private static final String TEAM_ID = "team_id";
    private static final String PROJECT_NAME = "Test Project";
    private static final String PROJECT_DESCRIPTION = "Test Description";
    private static final String UPDATED_PROJECT_NAME = "Updated Project";
    private static final String UPDATED_PROJECT_DESCRIPTION = "Updated Description";
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 12, 31);
    private static final String USER_EMAIL = "test@example.com";
    private static final String TEAM_NAME = "Test Team";

    // 자주 사용되는 테스트 객체들
    private Team team;
    private Project project;
    private TeamMember teamMember;

    // Request/Response 객체들
    private ProjectCreateRequest validCreateRequest;
    private ProjectCreateRequest createRequestWithoutFiles;
    private ProjectCreateRequest createRequestWithTooManyFiles;
    private ProjectCreateRequest createRequestWithLargeFiles;
    private ProjectCreateRequest createRequestWithUnsupportedFiles;
    private ProjectCreateRequest createRequestWithLongName;
    private ProjectCreateRequest createRequestWithInvalidDateRange;

    private ProjectUpdateRequest validUpdateRequest;
    private ProjectUpdateRequest updateRequestWithDeleteFiles;

    private ProjectSearchRequest searchRequest;

    // 파일 관련 객체들
    private List<MultipartFile> validFiles;
    private List<MultipartFile> emptyFiles;
    private List<MultipartFile> tooManyFiles;
    private List<MultipartFile> largeFiles;
    private List<MultipartFile> unsupportedFiles;

    // 페이징 관련
    private Pageable pageable;
    private Page<Project> projectPage;

    private Project projectFromDifferentTeam;

    // 진척도 리포트 관련
    private List<Map<String, Object>> validProgressReport;

    @BeforeEach
    void setUp() {
        // === 기본 Entity 객체들 ===
        setupBasicEntities();

        // === 파일 관련 객체들 ===
        setupFileObjects();

        // === Request/Response 객체들 ===
        setupRequestObjects();

        // === 페이징 관련 ===
        setupPagingObjects();

        // === 예외 테스트용 객체들 ===
        setupExceptionTestObjects();

        // === 진척도 리포트 관련 ===
        setupProgressReportObjects();
    }

    // ============ createProject 테스트들 ============

    @Test
    @DisplayName("프로젝트 생성 성공 - 파일 포함")
    void createProject_WithFiles_Success() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When
        ProjectDetailResponse response = projectService.createProject(validCreateRequest, USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(PROJECT_NAME);
        verify(projectFileService).uploadProjectFiles(any(Project.class), anyList());
        verify(projectRepository, times(2)).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 팀 멤버가 아닌 경우")
    void createProject_UserNotTeamMember_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(validCreateRequest, USER_ID))
                .isInstanceOf(TeamMemberNotMappedException.class);

        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 파일이 없는 경우")
    void createProject_NoFiles_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(createRequestWithoutFiles, USER_ID))
                .isInstanceOf(FileShouldNotBeNullException.class);
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 파일 개수 초과")
    void createProject_TooManyFiles_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(createRequestWithTooManyFiles, USER_ID))
                .isInstanceOf(FileCountExceededException.class);
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 파일 크기 초과")
    void createProject_FileSizeExceeded_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(createRequestWithLargeFiles, USER_ID))
                .isInstanceOf(FileSizeExceededException.class);
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 지원하지 않는 파일 형식")
    void createProject_UnsupportedFileFormat_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(createRequestWithUnsupportedFiles, USER_ID))
                .isInstanceOf(FileFormatNotSupportedException.class);
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 프로젝트명 너무 긴 경우")
    void createProject_ProjectNameTooLong_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> projectService.createProject(createRequestWithLongName, USER_ID))
                .isInstanceOf(ProjectNameTooLongException.class);
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 날짜 범위 잘못된 경우")
    void createProject_InvalidDateRange_ThrowsException() {
        // Given & When & Then
        assertThatThrownBy(() -> projectService.createProject(createRequestWithInvalidDateRange, USER_ID))
                .isInstanceOf(ProjectDateRangeInvalidException.class);
    }

    @Test
    @DisplayName("프로젝트 생성 실패 - 파일 업로드 중 예외 발생")
    void createProject_FileUploadFailed_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        doThrow(new FileUploadFailedException())
                .when(projectFileService).uploadProjectFiles(any(Project.class), anyList());

        // When & Then
        assertThatThrownBy(() -> projectService.createProject(validCreateRequest, USER_ID))
                .isInstanceOf(FileUploadFailedException.class);
    }

    // ============ getProject 테스트들 ============

    @Test
    @DisplayName("프로젝트 단건 조회 성공")
    void getProject_Success() {
        // Given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // When
        ProjectDetailResponse response = projectService.getProject(PROJECT_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(PROJECT_NAME);
        assertThat(response.getDescription()).isEqualTo(PROJECT_DESCRIPTION);
        assertThat(response.getStartDate()).isEqualTo(START_DATE);
        assertThat(response.getEndDate()).isEqualTo(END_DATE);
        verify(projectRepository).findById(PROJECT_ID);
    }

    @Test
    @DisplayName("프로젝트 단건 조회 실패 - 존재하지 않는 프로젝트")
    void getProject_ProjectNotFound_ThrowsException() {
        // Given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getProject(PROJECT_ID))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    @DisplayName("프로젝트 단건 조회 실패 - null ID 전달")
    void getProject_NullId_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> projectService.getProject(null))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    // ============ getProjects 테스트들 ============

    @Test
    @DisplayName("프로젝트 목록 조회 성공 - 검색어 있음")
    void getProjects_WithSearchKeyword_Success() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectCustomRepository.findByTeamAndProjectName(team, "Test", pageable))
                .thenReturn(projectPage);

        // When
        Page<ProjectResponse> response = projectService.getProjects(searchRequest, pageable, USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo(PROJECT_NAME);
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(projectCustomRepository).findByTeamAndProjectName(team, "Test", pageable);
    }

    @Test
    @DisplayName("프로젝트 목록 조회 성공 - 검색어 없음")
    void getProjects_WithoutSearchKeyword_Success() {
        // Given
        ProjectSearchRequest emptySearchRequest = new ProjectSearchRequest("");
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectCustomRepository.findByTeamAndProjectName(team, "", pageable))
                .thenReturn(projectPage);

        // When
        Page<ProjectResponse> response = projectService.getProjects(emptySearchRequest, pageable, USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(projectCustomRepository).findByTeamAndProjectName(team, "", pageable);
    }

    @Test
    @DisplayName("프로젝트 목록 조회 실패 - 팀 멤버가 아닌 경우")
    void getProjects_UserNotTeamMember_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.getProjects(searchRequest, pageable, USER_ID))
                .isInstanceOf(TeamMemberNotMappedException.class);

        verify(projectCustomRepository, never()).findByTeamAndProjectName(any(), any(), any());
    }

    @Test
    @DisplayName("프로젝트 목록 조회 성공 - 빈 결과")
    void getProjects_EmptyResult_Success() {
        // Given
        Page<Project> emptyProjectPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectCustomRepository.findByTeamAndProjectName(team, "Test", pageable))
                .thenReturn(emptyProjectPage);

        // When
        Page<ProjectResponse> response = projectService.getProjects(searchRequest, pageable, USER_ID);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    // ============ updateProject 테스트들 ============

    @Test
    @DisplayName("프로젝트 수정 성공 - 파일 포함")
    void updateProject_WithFiles_Success() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When
        ProjectDetailResponse response = projectService.updateProject(validUpdateRequest, USER_ID);

        // Then
        assertThat(response).isNotNull();
        verify(projectFileService).uploadProjectFiles(any(Project.class), anyList());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 수정 성공 - 파일 없이")
    void updateProject_WithoutFiles_Success() {
        // Given
        ProjectUpdateRequest requestWithoutFiles = ProjectUpdateRequest.builder()
                .id(PROJECT_ID)
                .name(UPDATED_PROJECT_NAME)
                .description(UPDATED_PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(Collections.emptyList())
                .deleteFileIds(Collections.emptyList())
                .build();

        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When
        ProjectDetailResponse response = projectService.updateProject(requestWithoutFiles, USER_ID);

        // Then
        assertThat(response).isNotNull();
        verify(projectFileService, never()).uploadProjectFiles(any(Project.class), anyList());
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 프로젝트 없음")
    void updateProject_ProjectNotFound_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(validUpdateRequest, USER_ID))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 권한 없음")
    void updateProject_NoAuthority_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(projectFromDifferentTeam));

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(validUpdateRequest, USER_ID))
                .isInstanceOf(UserHasNoAuthorityException.class);
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 팀 멤버가 아닌 경우")
    void updateProject_UserNotTeamMember_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(validUpdateRequest, USER_ID))
                .isInstanceOf(TeamMemberNotMappedException.class);
    }

    @Test
    @DisplayName("프로젝트 수정 성공 - 파일 삭제 포함")
    void updateProject_WithFileDelete_Success() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        // When
        ProjectDetailResponse response = projectService.updateProject(updateRequestWithDeleteFiles, USER_ID);

        // Then
        assertThat(response).isNotNull();
        verify(projectFileService).deleteProjectFile(1L);
        verify(projectFileService).deleteProjectFile(2L);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 파일 삭제 실패")
    void updateProject_FileDeleteFailed_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        doThrow(new RuntimeException("Delete failed"))
                .when(projectFileService).deleteProjectFile(1L);

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(updateRequestWithDeleteFiles, USER_ID))
                .isInstanceOf(FileDeleteFailedException.class);
    }

    @Test
    @DisplayName("프로젝트 수정 실패 - 파일 업로드 실패")
    void updateProject_FileUploadFailed_ThrowsException() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        doThrow(new RuntimeException("Upload failed"))
                .when(projectFileService).uploadProjectFiles(any(Project.class), anyList());

        // When & Then
        assertThatThrownBy(() -> projectService.updateProject(validUpdateRequest, USER_ID))
                .isInstanceOf(FileUploadFailedException.class);
    }

    // ============ deleteProject 테스트들 ============

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void deleteProject_Success() {
        // Given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // When
        projectService.deleteProject(PROJECT_ID);

        // Then
        verify(projectFileService).deleteAllProjectFiles(PROJECT_ID);
        verify(projectRepository).delete(project);
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 프로젝트 없음")
    void deleteProject_ProjectNotFound_ThrowsException() {
        // Given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(PROJECT_ID))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectFileService, never()).deleteAllProjectFiles(any());
        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - 파일 삭제 실패")
    void deleteProject_FileDeleteFailed_ThrowsException() {
        // Given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        doThrow(new RuntimeException("Delete failed"))
                .when(projectFileService).deleteAllProjectFiles(PROJECT_ID);

        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(PROJECT_ID))
                .isInstanceOf(FileDeleteFailedException.class);

        verify(projectRepository, never()).delete(any());
    }

    @Test
    @DisplayName("프로젝트 삭제 실패 - null ID")
    void deleteProject_NullId_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> projectService.deleteProject(null))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    // ============ updateProjectProgress 테스트들 ============

    @Test
    @DisplayName("프로젝트 진척도 업데이트 성공")
    void updateProjectProgress_Success() {
        // Given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // When
        projectService.updateProjectProgress(validProgressReport);

        // Then
        verify(projectRepository).findById(PROJECT_ID);
        assertThat(project.getProgress()).isEqualTo(75);
    }

    @Test
    @DisplayName("프로젝트 진척도 업데이트 실패 - 프로젝트 없음")
    void updateProjectProgress_ProjectNotFound_ThrowsException() {
        // Given
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> projectService.updateProjectProgress(validProgressReport))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    @DisplayName("프로젝트 진척도 업데이트 실패 - 빈 리포트")
    void updateProjectProgress_EmptyReport_Success() {
        // Given
        List<Map<String, Object>> emptyReport = Collections.emptyList();

        // When
        projectService.updateProjectProgress(emptyReport);

        // Then
        verify(projectRepository, never()).findById(any());
    }

    @Test
    @DisplayName("프로젝트 진척도 업데이트 실패 - null 리포트")
    void updateProjectProgress_NullReport_ThrowsException() {
        // When & Then
        assertThatThrownBy(() -> projectService.updateProjectProgress(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ============ 통합 테스트들 ============

    @Test
    @DisplayName("통합 테스트 - 프로젝트 생성 후 조회")
    void integrationTest_CreateAndGetProject() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // When - 프로젝트 생성
        ProjectDetailResponse createResponse = projectService.createProject(validCreateRequest, USER_ID);

        // Then - 생성 검증
        assertThat(createResponse).isNotNull();
        assertThat(createResponse.getName()).isEqualTo(PROJECT_NAME);

        // When - 프로젝트 조회
        ProjectDetailResponse getResponse = projectService.getProject(PROJECT_ID);

        // Then - 조회 검증
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.getName()).isEqualTo(PROJECT_NAME);
        assertThat(getResponse.getDescription()).isEqualTo(PROJECT_DESCRIPTION);
    }

    @Test
    @DisplayName("통합 테스트 - 프로젝트 생성 후 수정")
    void integrationTest_CreateAndUpdateProject() {
        // Given
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));

        // When - 프로젝트 생성
        ProjectDetailResponse createResponse = projectService.createProject(validCreateRequest, USER_ID);

        // Then - 생성 검증
        assertThat(createResponse).isNotNull();

        // When - 프로젝트 수정
        ProjectDetailResponse updateResponse = projectService.updateProject(validUpdateRequest, USER_ID);

        // Then - 수정 검증
        assertThat(updateResponse).isNotNull();
        verify(projectRepository, times(3)).save(any(Project.class)); // 생성 2번 + 수정 1번
    }

    private void setupBasicEntities() {
        // User 객체
        Users user = Users.builder()
                .id(USER_ID)
                .email(USER_EMAIL)
                .name("Test User")
                .build();

        // Team 객체
        team = Team.builder()
                .id(TEAM_ID)
                .name(TEAM_NAME)
                .build();

        // TeamMember 객체
        teamMember = TeamMember.builder()
                .id(1L)
                .team(team)
                .user(user)
                .build();

        // Project 객체
        project = Project.builder()
                .id(PROJECT_ID)
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .team(team)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .progress(0)
                .build();
    }

    private void setupFileObjects() {
        // 개별 파일 객체들
        MockMultipartFile validDocxFile = new MockMultipartFile(
                "file", "test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[1024]
        );

        MockMultipartFile validXlsxFile = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[2048]
        );

        MockMultipartFile validTxtFile = new MockMultipartFile(
                "file", "test.txt", "text/plain",
                new byte[512]
        );

        // 60MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[60 * 1024 * 1024] // 60MB
        );

        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file", "test.pdf", "application/pdf",
                new byte[1024]
        );

        // 파일 리스트들
        validFiles = List.of(validDocxFile, validXlsxFile);
        emptyFiles = Collections.emptyList();
        tooManyFiles = IntStream.range(0, 6)
                .mapToObj(i -> new MockMultipartFile("file", "test" + i + ".docx",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[1024]))
                .collect(Collectors.toList());
        largeFiles = List.of(largeFile);
        unsupportedFiles = List.of(unsupportedFile);
    }

    private void setupRequestObjects() {
        // ProjectCreateRequest 객체들
        validCreateRequest = ProjectCreateRequest.builder()
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(validFiles)
                .build();

        createRequestWithoutFiles = ProjectCreateRequest.builder()
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(emptyFiles)
                .build();

        createRequestWithTooManyFiles = ProjectCreateRequest.builder()
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(tooManyFiles)
                .build();

        createRequestWithLargeFiles = ProjectCreateRequest.builder()
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(largeFiles)
                .build();

        createRequestWithUnsupportedFiles = ProjectCreateRequest.builder()
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(unsupportedFiles)
                .build();

        createRequestWithLongName = ProjectCreateRequest.builder()
                .name("a".repeat(101)) // 100자 초과
                .description(PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(validFiles)
                .build();

        createRequestWithInvalidDateRange = ProjectCreateRequest.builder()
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .startDate(END_DATE) // 시작일이 종료일보다 늦음
                .endDate(START_DATE)
                .files(validFiles)
                .build();

        // ProjectUpdateRequest 객체들
        validUpdateRequest = ProjectUpdateRequest.builder()
                .id(PROJECT_ID)
                .name(UPDATED_PROJECT_NAME)
                .description(UPDATED_PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(validFiles)
                .deleteFileIds(Collections.emptyList())
                .build();

        updateRequestWithDeleteFiles = ProjectUpdateRequest.builder()
                .id(PROJECT_ID)
                .name(UPDATED_PROJECT_NAME)
                .description(UPDATED_PROJECT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .files(Collections.emptyList())
                .deleteFileIds(List.of(1L, 2L))
                .build();

        // ProjectSearchRequest
        searchRequest = new ProjectSearchRequest("Test");
    }

    private void setupPagingObjects() {
        pageable = PageRequest.of(0, 10);
        List<Project> projects = List.of(project);
        projectPage = new PageImpl<>(projects, pageable, 1);
    }

    private void setupExceptionTestObjects() {
        // 다른 팀 객체들 (권한 없음 테스트용)
        // 예외 관련
        Team differentTeam = Team.builder()
                .id("different_team_id")
                .name("Different Team")
                .build();

        projectFromDifferentTeam = Project.builder()
                .id(PROJECT_ID)
                .name(PROJECT_NAME)
                .description(PROJECT_DESCRIPTION)
                .team(differentTeam)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .build();

        TeamMember differentTeamMember = TeamMember.builder()
                .id(2L)
                .team(differentTeam)
                .build();
    }

    private void setupProgressReportObjects() {
        // 진척도 리포트 관련
        Map<String, Object> teamProgress = Map.of("overall_progress", 75);
        Map<String, Object> progressReportItem = Map.of(
                "project_id", PROJECT_ID,
                "team_progress_overview", teamProgress
        );
        validProgressReport = List.of(progressReportItem);

        // 잘못된 진척도 리포트
        Map<String, Object> invalidTeamProgress = Map.of("overall_progress", "invalid");
        Map<String, Object> invalidProgressItem = Map.of(
                "project_id", "invalid",
                "team_progress_overview", invalidTeamProgress
        );
        List<Map<String, Object>> invalidProgressReport = List.of(invalidProgressItem);
    }
}
