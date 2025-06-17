package com.yaxim.report.service;

import com.yaxim.dashboard.comment.service.CommentService;
import com.yaxim.global.for_ai.dto.request.DailyReportCreateRequest;
import com.yaxim.report.controller.dto.response.DailyReportDetailResponse;
import com.yaxim.report.controller.dto.response.DailyReportResponse;
import com.yaxim.report.entity.UserDailyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.UserDailyReportRepository;
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

@Service
@RequiredArgsConstructor
public class UserDailyReportService {

    private final UserDailyReportRepository dailyReportRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CommentService commentService;

    @Transactional
    public DailyReportDetailResponse createDailyReport(Long userId, DailyReportCreateRequest request) {
        Users user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        TeamMember teamMember = teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new);

        UserDailyReport report = UserDailyReport.builder()
                .date(request.getDate())
                .report(request.getReport())
                .user(user)
                .team(teamMember.getTeam())
                .build();

        dailyReportRepository.save(report);

        commentService.addComment(
                user,
                (String) report.getReport()
                        .getOrDefault("daily_short_review", "")
        );

        return DailyReportDetailResponse.from(report);
    }

    @Transactional(readOnly = true)
    public Page<DailyReportResponse> getMyDailyReports(Long userId, Pageable pageable) {
        return dailyReportRepository.findByUserId(userId, pageable)
                .map(DailyReportResponse::from);
    }

    @Transactional(readOnly = true)
    public DailyReportDetailResponse getReportById(Long reportId, Long userId) {
        UserDailyReport report = dailyReportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportAccessDeniedException();
        }
        return DailyReportDetailResponse.from(report);
    }

    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        UserDailyReport report = dailyReportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportAccessDeniedException();
        }
        dailyReportRepository.delete(report);
    }
}
