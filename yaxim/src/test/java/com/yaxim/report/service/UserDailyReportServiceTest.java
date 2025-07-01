package com.yaxim.report.service;

import com.yaxim.dashboard.comment.service.CommentService;
import com.yaxim.global.for_ai.dto.request.DailyReportCreateRequest;
import com.yaxim.global.for_ai.dto.request.UserDailyListRequest;
import com.yaxim.report.controller.dto.response.DailyReportDetailResponse;
import com.yaxim.report.controller.dto.response.DailyReportResponse;
import com.yaxim.report.entity.UserDailyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.UserDailyReportRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDailyReportServiceTest {
    @Mock
    private UserDailyReportRepository dailyReportRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private CommentService commentService;

    @InjectMocks
    private UserDailyReportService userDailyReportService;

    private static final Long USER_ID = 1L;
    private static final String TEAM_ID = "test_team_id";
    private static final Long REPORT_ID = 100L;
    private static final Long WRONG_USER_ID = 2L;
    private static final LocalDate DATE = LocalDate.of(2025, 6, 30);

    private Users user;
    private TeamMember teamMember;
    private UserDailyReport report;
    private DailyReportCreateRequest request;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new Users(
                USER_ID,
                "name",
                "email@test.com"
        );

        Team team = new Team(
                TEAM_ID,
                "test",
                "description"
        );

        teamMember = new TeamMember(
                team,
                user,
                UserRole.MEMBER
        );

        Map<String, Object> dailyReportMap = new HashMap<>();
        dailyReportMap.put("summary", "오늘 업무 요약");

        Map<String, Object> dailyReport = new HashMap<>();
        dailyReport.put("report_title", "일일 보고서 제목");
        dailyReport.put("daily_report", dailyReportMap);

        request = new DailyReportCreateRequest(
                USER_ID,
                DATE,
                dailyReport
        );

        report = new UserDailyReport(
                REPORT_ID,
                DATE,
                dailyReport,
                user,
                team
        );

        pageable = Pageable.ofSize(10);
    }

    @Test
    @DisplayName("데일리 보고서 생성")
    void createDailyReportTest() {
        // given
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(any(Long.class))).thenReturn(Optional.of(teamMember));
        when(dailyReportRepository.save(any(UserDailyReport.class))).thenReturn(report);

        // when
        DailyReportDetailResponse response = userDailyReportService.createDailyReport(request.getUserId(), request);

        // then
        assertEquals(request.getDate(), response.getDate());
    }

    @Test
    @DisplayName("내 데일리 보고서 목록 조회")
    void getMyDailyReportsTest() {
        // given
        List<UserDailyReport> reportList = List.of(report);
        Page<UserDailyReport> reportPage = new PageImpl<>(reportList, pageable, 1);
        when(dailyReportRepository.findByUserId(USER_ID, pageable))
                .thenReturn(reportPage);

        // when
        Page<DailyReportResponse> response = userDailyReportService.getMyDailyReports(USER_ID, pageable);

        // then
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());
    }

    @Test
    @DisplayName("내 데일리 보고서 목록 조회 없을 때 예외처리")
    void getMyEmptyDailyReportsTest() {
        // given
        when(dailyReportRepository.findByUserId(USER_ID, pageable))
                .thenReturn(Page.empty());

        // when
        Page<DailyReportResponse> response = userDailyReportService.getMyDailyReports(USER_ID, pageable);

        // then
        assertEquals(0, response.getTotalElements());
    }

    @Test
    @DisplayName("리포트 ID로 조회 - 성공 케이스")
    void getReportByIdSuccessTest() {
        // given
        when(dailyReportRepository.findById(REPORT_ID))
                .thenReturn(Optional.of(report));

        // when
        DailyReportDetailResponse response = userDailyReportService.getReportById(REPORT_ID, USER_ID);

        // then
        assertEquals(REPORT_ID, response.getId());
    }

    @Test
    @DisplayName("리포트 ID로 조회 - 리포트를 찾을 수 없는 경우")
    void getReportByIdNotFoundTest() {
        // given
        when(dailyReportRepository.findById(REPORT_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(ReportNotFoundException.class, () ->
                userDailyReportService.getReportById(REPORT_ID, USER_ID));
    }

    @Test
    @DisplayName("리포트 ID로 조회 - 사용자 ID가 일치하지 않는 경우")
    void getReportByIdUserIdMismatchTest() {
        // given
        when(dailyReportRepository.findById(REPORT_ID))
                .thenReturn(Optional.of(report));

        // when & then
        assertThrows(ReportAccessDeniedException.class, () ->
                userDailyReportService.getReportById(REPORT_ID, WRONG_USER_ID));
    }

    @Test
    @DisplayName("데일리 보고서 삭제")
    void deleteReportTest() {
        // given
        when(dailyReportRepository.findById(REPORT_ID))
                .thenReturn(Optional.of(report));

        // when & then
        userDailyReportService.deleteReport(REPORT_ID, USER_ID);
        verify(dailyReportRepository).findById(REPORT_ID);
        verify(dailyReportRepository).delete(report);
    }

    @Test
    @DisplayName("데일리 보고서 삭제 중 보고서 찾지 못했을 때 예외처리")
    void deleteReportTest_ReportNotFound() {
        // given
        when(dailyReportRepository.findById(REPORT_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(ReportNotFoundException.class, () ->
                userDailyReportService.deleteReport(REPORT_ID, USER_ID));
    }

    @Test
    @DisplayName("AI 전용 함수 - 개인 데일리 보고서 리스트 불러오기")
    void getUserDailyReportValidTest() {
        // given
        UserDailyListRequest request = new UserDailyListRequest(USER_ID, DATE, DATE.plusDays(1));
        List<UserDailyReport> reportList = List.of(report);

        when(dailyReportRepository.findByUserIdAndDateBetween(
                request.getUserId(),
                request.getStartDate(),
                request.getEndDate()))
                .thenReturn(reportList);

        // when
        List<DailyReportDetailResponse> responseList =
                userDailyReportService.getUserDailyReport(request);

        // then
        assertEquals(reportList.size(), responseList.size());
        assertEquals(
                reportList.get(0).getDate(), responseList.get(0).getDate());
        assertEquals(
                reportList.get(0).getReport(), responseList.get(0).getReport());
    }

    @Test
    @DisplayName("AI 전용 함수 - 보고서 없을 때 예외처리")
    void getUserDailyReportNoReportsTest() {
        // given
        UserDailyListRequest request = new UserDailyListRequest(USER_ID, DATE, DATE.plusDays(1));
        List<UserDailyReport> reportList = List.of();

        when(dailyReportRepository.findByUserIdAndDateBetween(
                request.getUserId(),
                request.getStartDate(),
                request.getEndDate()))
                .thenReturn(reportList);

        // when
        List<DailyReportDetailResponse> responseList =
                userDailyReportService.getUserDailyReport(request);

        // then
        assertEquals(0, responseList.size());
    }
}
