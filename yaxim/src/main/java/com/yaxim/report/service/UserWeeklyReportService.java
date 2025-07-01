package com.yaxim.report.service;

import com.yaxim.global.for_ai.dto.request.UserWeeklyListRequest;
import com.yaxim.global.for_ai.dto.request.WeeklyReportCreateRequest;
import com.yaxim.report.controller.dto.response.WeeklyReportDetailResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportResponse;
import com.yaxim.report.entity.UserWeeklyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.UserWeeklyReportRepository;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional import
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserWeeklyReportService {

    private final UserWeeklyReportRepository weeklyReportRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public WeeklyReportDetailResponse createWeeklyReport(Long userId, WeeklyReportCreateRequest request) {
        Users user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        TeamMember teamMember = teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new);

        UserWeeklyReport report = UserWeeklyReport.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .report(request.getReport())
                .user(user)
                .team(teamMember.getTeam())
                .build();

        weeklyReportRepository.save(report);
        return WeeklyReportDetailResponse.from(report);
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReportResponse> getMyWeeklyReports(Long userId, Pageable pageable) {
        return weeklyReportRepository.findByUserId(userId, pageable)
                .map(WeeklyReportResponse::from);
    }

    @Transactional(readOnly = true)
    public WeeklyReportDetailResponse getReportById(Long reportId, Long userId) {
        UserWeeklyReport report = weeklyReportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportAccessDeniedException();
        }
        return WeeklyReportDetailResponse.from(report);
    }

    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        UserWeeklyReport report = weeklyReportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportAccessDeniedException();
        }
        weeklyReportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public List<WeeklyReportDetailResponse> getUserWeeklyReport(
            UserWeeklyListRequest request
    ) {
        List<UserWeeklyReport> reports = weeklyReportRepository.findByTeamIdAndStartDateAndEndDate(
                request.getTeamId(),
                request.getStartDate(),
                request.getEndDate()
        );

        return reports.stream()
                .map(WeeklyReportDetailResponse::from)
                .toList();
    }
}
