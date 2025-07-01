package com.yaxim.report.service;

import com.yaxim.global.for_ai.dto.request.UserWeeklyListRequest;
import com.yaxim.global.for_ai.dto.request.WeeklyReportCreateRequest;
import com.yaxim.report.controller.dto.response.WeeklyReportDetailResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportResponse;
import com.yaxim.report.entity.UserWeeklyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.UserWeeklyReportRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserWeeklyReportServiceTest {

    @Mock
    private UserWeeklyReportRepository weeklyReportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;

    @InjectMocks
    private UserWeeklyReportService userWeeklyReportService;

    private static final Long USER_ID = 1L;
    private static final Long REPORT_ID = 100L;
    private static final String TEAM_ID = "team_id";
    private static final LocalDate START_DATE = LocalDate.of(2024, 1, 1);
    private static final LocalDate END_DATE = LocalDate.of(2024, 1, 7);

    private Users user;
    private Team team;
    private TeamMember teamMember;
    private UserWeeklyReport weeklyReport;
    private WeeklyReportCreateRequest createRequest;
    private UserWeeklyListRequest listRequest;
    private Pageable pageable;
    private Page<UserWeeklyReport> reportPage;

    @BeforeEach
    void setUp() {
        user = new Users(
                USER_ID,
                "example@test.com",
                "Test User"
        );
        team = new Team(TEAM_ID, "Test Team", "description");
        teamMember = new TeamMember(team, user, UserRole.MEMBER);

        Map<String, Object> reportData = new HashMap<>();
        reportData.put("summary", "주간 업무 요약");

        weeklyReport = new UserWeeklyReport(
                REPORT_ID,
                START_DATE,
                END_DATE,
                reportData,
                user,
                team
        );

        createRequest = new WeeklyReportCreateRequest(
                USER_ID,
                START_DATE,
                END_DATE,
                reportData
        );

        listRequest = new UserWeeklyListRequest(
                TEAM_ID,
                START_DATE,
                END_DATE
        );

        pageable = PageRequest.of(0, 10);
        reportPage = new PageImpl<>(List.of(weeklyReport), pageable, 1);
    }

    @Test
    @DisplayName("주간 보고서 생성 성공")
    void createWeeklyReport_Success() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));

        WeeklyReportDetailResponse response = userWeeklyReportService.createWeeklyReport(USER_ID, createRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStartDate()).isEqualTo(START_DATE);
    }

    @Test
    @DisplayName("주간 보고서 생성 실패 - 사용자 없음")
    void createWeeklyReport_UserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userWeeklyReportService.createWeeklyReport(USER_ID, createRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("주간 보고서 생성 실패 - 팀 멤버 아님")
    void createWeeklyReport_NotTeamMember() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userWeeklyReportService.createWeeklyReport(USER_ID, createRequest))
                .isInstanceOf(TeamMemberNotMappedException.class);
    }

    @Test
    @DisplayName("내 주간 보고서 목록 조회 성공")
    void getMyWeeklyReports_Success() {
        when(weeklyReportRepository.findByUserId(USER_ID, pageable)).thenReturn(reportPage);

        Page<WeeklyReportResponse> response = userWeeklyReportService.getMyWeeklyReports(USER_ID, pageable);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("주간 보고서 단건 조회 성공")
    void getReportById_Success() {
        when(weeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(weeklyReport));

        WeeklyReportDetailResponse response = userWeeklyReportService.getReportById(REPORT_ID, USER_ID);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("주간 보고서 단건 조회 실패 - 보고서 없음")
    void getReportById_ReportNotFound() {
        when(weeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userWeeklyReportService.getReportById(REPORT_ID, USER_ID))
                .isInstanceOf(ReportNotFoundException.class);
    }

    @Test
    @DisplayName("주간 보고서 단건 조회 실패 - 접근 권한 없음")
    void getReportById_AccessDenied() {
        Users otherUser = new Users(999L, "other@example.com", "Other User");
        UserWeeklyReport otherReport = new UserWeeklyReport(REPORT_ID, START_DATE, END_DATE, new HashMap<>(), otherUser, team);

        when(weeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(otherReport));

        assertThatThrownBy(() -> userWeeklyReportService.getReportById(REPORT_ID, USER_ID))
                .isInstanceOf(ReportAccessDeniedException.class);
    }

    @Test
    @DisplayName("주간 보고서 삭제 성공")
    void deleteReport_Success() {
        when(weeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(weeklyReport));

        userWeeklyReportService.deleteReport(REPORT_ID, USER_ID);

        verify(weeklyReportRepository).delete(weeklyReport);
    }

    @Test
    @DisplayName("주간 보고서 삭제 실패 - 접근 권한 없음")
    void deleteReport_AccessDenied() {
        Users otherUser = new Users(999L, "other@example.com", "Other User");
        UserWeeklyReport otherReport = new UserWeeklyReport(REPORT_ID, START_DATE, END_DATE, new HashMap<>(), otherUser, team);

        when(weeklyReportRepository.findById(REPORT_ID)).thenReturn(Optional.of(otherReport));

        assertThatThrownBy(() -> userWeeklyReportService.deleteReport(REPORT_ID, USER_ID))
                .isInstanceOf(ReportAccessDeniedException.class);
    }

    @Test
    @DisplayName("특정 팀의 주간 보고서 목록 조회 성공")
    void getUserWeeklyReport_Success() {
        when(weeklyReportRepository.findByTeamIdAndStartDateAndEndDate(TEAM_ID, START_DATE, END_DATE))
                .thenReturn(List.of(weeklyReport));

        List<WeeklyReportDetailResponse> responses = userWeeklyReportService.getUserWeeklyReport(listRequest);

        assertThat(responses).hasSize(1);
    }
}
