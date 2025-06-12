package com.yaxim.report.service;

import com.yaxim.report.controller.dto.request.ReportCreateRequest;
import com.yaxim.report.controller.dto.response.ReportResponse;
import com.yaxim.report.entity.PersonalDailyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.PersonalDailyReportRepository;
import com.yaxim.report.util.JsonConverter;
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
public class PersonalDailyReportService {

    private final PersonalDailyReportRepository dailyReportRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public ReportResponse createDailyReport(Long userId, ReportCreateRequest request) {
        Users user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        TeamMember teamMember = teamMemberRepository.findByEmail(user.getEmail()).orElseThrow(TeamMemberNotMappedException::new);

        PersonalDailyReport report = PersonalDailyReport.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .report(request.getReport())
                .user(user)
                .team(teamMember.getTeam())
                .build();

        dailyReportRepository.save(report);
        return convertToResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getMyDailyReports(Long userId, Pageable pageable) {
        Page<ReportResponse> personalDailyReports = dailyReportRepository.findByUserId(userId, pageable).map(this::convertToResponse);



        return personalDailyReports;
    }

    public ReportResponse getReportById(Long reportId, Long userId) {
        PersonalDailyReport report = dailyReportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportAccessDeniedException();
        }
        return convertToResponse(report);
    }

    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        PersonalDailyReport report = dailyReportRepository.findById(reportId).orElseThrow(ReportNotFoundException::new);
        if (!report.getUser().getId().equals(userId)) {
            throw new ReportAccessDeniedException();
        }
        dailyReportRepository.delete(report);
    }

    private ReportResponse convertToResponse(PersonalDailyReport report) {
        return ReportResponse.builder()
                .id(report.getId())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .report(JsonConverter.parseStringToObject(report.getReport()))
                .userId(report.getUser().getId())
                .userName(report.getUser().getName())
                .teamId(report.getTeam().getId())
                .build();
    }
}
