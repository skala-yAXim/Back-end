package com.yaxim.report.service;

import com.yaxim.dashboard.comment.service.CommentService;
import com.yaxim.global.for_ai.dto.request.TeamWeeklyReportCreateRequest;
import com.yaxim.project.service.ProjectService;
import com.yaxim.report.controller.dto.request.TeamMemberWeeklyPageRequest;
import com.yaxim.report.controller.dto.response.TeamMemberWeeklyDetailResponse;
import com.yaxim.report.controller.dto.response.TeamMemberWeeklyReportResponse;
import com.yaxim.report.controller.dto.response.TeamWeeklyDetailResponse;
import com.yaxim.report.controller.dto.response.TeamWeeklyReportResponse;
import com.yaxim.report.entity.TeamWeeklyReport;
import com.yaxim.report.entity.UserWeeklyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.TeamMemberWeeklyCustomRepository;
import com.yaxim.report.repository.TeamWeeklyReportRepository;
import com.yaxim.report.repository.UserWeeklyReportRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.repository.TeamRepository;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TeamWeeklyReportServiceTest {

    @Mock
    private TeamWeeklyReportRepository teamWeeklyReportRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private UserWeeklyReportRepository userWeeklyReportRepository;
    @Mock
    private TeamMemberWeeklyCustomRepository teamMemberWeeklyCustomRepository;
    @Mock
    private CommentService commentService;
    @Mock
    private ProjectService projectService;

    @InjectMocks private TeamWeeklyReportService teamWeeklyReportService;

    private static final Long USER_ID = 1L;
    private static final String TEAM_ID = "test_team_id";
    private static final Long REPORT_ID = 100L;
    private static final LocalDate START_DATE = LocalDate.of(2025, 6, 30);
    private static final LocalDate END_DATE = LocalDate.of(2025, 7, 6);

    private Team team;
    private TeamMember teamMember;
    private TeamWeeklyReport teamWeeklyReport;
    private UserWeeklyReport userWeeklyReport;
    private TeamWeeklyReportCreateRequest createRequest;
    private TeamMemberWeeklyPageRequest pageRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        team = new Team(
                TEAM_ID,
                "Test Team",
                "test"
        );

        Users user = new Users(
                1L,
                "name",
                "email@test.com"
        );

        teamMember = new TeamMember(
                team,
                user,
                UserRole.LEADER
        );

        Map<String, Object> reportData = Map.of(
                "weekly_short_review", "Good week",
                "team_weekly_report", List.of(Map.of("project_id", 1L, "progress", 75))
        );

        Map<String, Object> memberReportData = Map.of(
                "weekly_report", List.of(Map.of("report_title", "title"))
        );

        teamWeeklyReport = new TeamWeeklyReport(
                REPORT_ID,
                START_DATE,
                END_DATE,
                reportData,
                team
        );

        userWeeklyReport = new UserWeeklyReport(
                START_DATE,
                END_DATE,
                memberReportData,
                user,
                team
        );

        createRequest = new TeamWeeklyReportCreateRequest(TEAM_ID, START_DATE, END_DATE, reportData);

        pageRequest = new TeamMemberWeeklyPageRequest(
                List.of(user.getId()),
                START_DATE,
                END_DATE
        );

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("팀 주간 보고서 생성 성공")
    void createTeamWeeklyReport_Success() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(teamWeeklyReportRepository.save(any(TeamWeeklyReport.class))).thenReturn(teamWeeklyReport);

        TeamWeeklyReportResponse response = teamWeeklyReportService.createTeamWeeklyReport(createRequest);

        assertThat(response).isNotNull();
        verify(commentService).addComment(eq(team), eq("Good week"));
        verify(projectService).updateProjectProgress(anyList());
    }

    @Test
    @DisplayName("팀 주간 보고서 생성 실패 - 팀 없음")
    void createTeamWeeklyReport_TeamNotFound() {
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamWeeklyReportService.createTeamWeeklyReport(createRequest))
                .isInstanceOf(TeamMemberNotMappedException.class);
    }

    @Test
    @DisplayName("팀 주간 보고서 목록 조회 성공")
    void getTeamWeeklyReport_Success() {
        Page<TeamWeeklyReport> reportPage = new PageImpl<>(List.of(teamWeeklyReport));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamWeeklyReportRepository.findByTeam(team, pageable)).thenReturn(reportPage);

        Page<TeamWeeklyReportResponse> response = teamWeeklyReportService.getTeamWeeklyReport(USER_ID, pageable);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("팀 주간 보고서 목록 조회 실패 - 팀 멤버 아님")
    void getTeamWeeklyReport_NotTeamMember() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamWeeklyReportService.getTeamWeeklyReport(USER_ID, pageable))
                .isInstanceOf(TeamMemberNotMappedException.class);
    }

    @Test
    @DisplayName("팀 주간 보고서 단건 조회 성공")
    void getReportById_Success() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamWeeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(teamWeeklyReport));

        TeamWeeklyDetailResponse response = teamWeeklyReportService.getReportById(REPORT_ID, USER_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("팀 주간 보고서 단건 조회 실패 - 보고서 없음")
    void getReportById_ReportNotFound() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamWeeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamWeeklyReportService.getReportById(REPORT_ID, USER_ID))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    @DisplayName("팀 주간 보고서 단건 조회 실패 - 접근 권한 없음")
    void getReportById_AccessDenied() {
        Team differentTeam = new Team("different_team_id", "Different Team", "");
        TeamWeeklyReport differentReport = new TeamWeeklyReport(REPORT_ID, START_DATE, END_DATE, Map.of(), differentTeam);

        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamWeeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(differentReport));

        assertThatThrownBy(() -> teamWeeklyReportService.getReportById(REPORT_ID, USER_ID))
                .isInstanceOf(ReportAccessDeniedException.class);
    }

    @Test
    @DisplayName("팀 주간 보고서 삭제 성공")
    void deleteReport_Success() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamWeeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(teamWeeklyReport));

        teamWeeklyReportService.deleteReport(REPORT_ID, USER_ID);

        verify(teamWeeklyReportRepository).delete(teamWeeklyReport);
    }

    @Test
    @DisplayName("팀 주간 보고서 삭제 실패 - 접근 권한 없음")
    void deleteReport_AccessDenied() {
        Team differentTeam = new Team("different_team_id", "Different Team", "");
        TeamWeeklyReport differentReport = new TeamWeeklyReport(REPORT_ID, START_DATE, END_DATE, Map.of(), differentTeam);

        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamWeeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(differentReport));

        assertThatThrownBy(() -> teamWeeklyReportService.deleteReport(REPORT_ID, USER_ID))
                .isInstanceOf(ReportAccessDeniedException.class);
    }

    @Test
    @DisplayName("팀 멤버 주간 보고서 목록 조회 성공")
    void getTeamMemberWeeklyReports_Success() {
        Page<UserWeeklyReport> reportPage = new PageImpl<>(List.of(userWeeklyReport));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamMemberWeeklyCustomRepository.findTeamMemberWeekly(pageRequest, team, pageable))
                .thenReturn(reportPage);

        Page<TeamMemberWeeklyReportResponse> response =
                teamWeeklyReportService.getTeamMemberWeeklyReports(pageRequest, pageable, USER_ID);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("팀 멤버 주간 보고서 단건 조회 성공")
    void getTeamMemberWeeklyReport_Success() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(userWeeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(userWeeklyReport));

        TeamMemberWeeklyDetailResponse response =
                teamWeeklyReportService.getTeamMemberWeeklyReport(REPORT_ID, USER_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("팀 멤버 주간 보고서 단건 조회 실패 - 접근 권한 없음")
    void getTeamMemberWeeklyReport_AccessDenied() {
        Team differentTeam = new Team("different_team_id", "Different Team", "");
        UserWeeklyReport differentReport = new UserWeeklyReport(
                START_DATE,
                END_DATE,
                Map.of(
                        "weekly_report", List.of(Map.of("report_title", "title"))
                ),
                new Users(USER_ID, "name", "email@test.com"),
                team
        );

        when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(
                        new TeamMember(
                                differentTeam,
                                teamMember.getUser(),
                                UserRole.MEMBER
                        )
                ));
        when(userWeeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(differentReport));

        assertThatThrownBy(() -> teamWeeklyReportService.getTeamMemberWeeklyReport(REPORT_ID, USER_ID))
                .isInstanceOf(ReportAccessDeniedException.class);
    }
}
