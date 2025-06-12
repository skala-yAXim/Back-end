package com.yaxim.report.service;

import com.yaxim.report.controller.dto.request.ReportCreateRequest;
import com.yaxim.report.controller.dto.response.ReportResponse;
import com.yaxim.report.entity.TeamWeeklyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.TeamWeeklyReportRepository;
import com.yaxim.report.util.JsonConverter;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional import

@Service
@RequiredArgsConstructor
public class TeamWeeklyReportService {

    private final TeamWeeklyReportRepository teamWeeklyReportRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public ReportResponse createTeamWeeklyReport(Long userId, ReportCreateRequest request) {
        TeamMember creator = validateLeaderAndGetTeamMember(userId);

        TeamWeeklyReport report = TeamWeeklyReport.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .report(request.getReport())
                .team(creator.getTeam())
                .build();

        teamWeeklyReportRepository.save(report);
        return convertToResponse(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getTeamWeeklyReports(Long userId, LocalDate start, LocalDate end, Pageable pageable) {
        // 검증 로직을 헬퍼 메서드로 통합
        TeamMember viewer = validateLeaderAndGetTeamMember(userId);

        String teamId = viewer.getTeam().getId();
        return teamWeeklyReportRepository.findByTeamIdAndDateRange(teamId, start, end, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long reportId, Long userId) {
        // 1. 요청자의 리더 권한 및 팀 정보 확인
        TeamMember viewer = validateLeaderAndGetTeamMember(userId);

        // 2. 보고서 조회
        TeamWeeklyReport report = teamWeeklyReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        // 3. 요청자가 해당 보고서의 팀에 속한 리더인지 확인
        if (!report.getTeam().getId().equals(viewer.getTeam().getId())) {
            throw new ReportAccessDeniedException();
        }
        return convertToResponse(report);
    }

    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        // 1. 요청자의 리더 권한 및 팀 정보 확인
        TeamMember deleter = validateLeaderAndGetTeamMember(userId);

        // 2. 보고서 조회
        TeamWeeklyReport report = teamWeeklyReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        // 3. 요청자가 해당 보고서의 팀에 속한 리더인지 확인
        if (!report.getTeam().getId().equals(deleter.getTeam().getId())) {
            throw new ReportAccessDeniedException();
        }
        teamWeeklyReportRepository.delete(report);
    }

    /**
     * 사용자가 팀의 리더인지 검증하고, 유효한 경우 TeamMember 객체를 반환하는 헬퍼 메서드
     * @param userId 검증할 사용자의 ID
     * @return 검증된 TeamMember 객체
     */
    private TeamMember validateLeaderAndGetTeamMember(Long userId) {
        // 1. 사용자 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 사용자의 이메일로 팀 멤버 정보 조회 (팀 소속 여부 확인)
        TeamMember teamMember = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new TeamMemberNotMappedException());

        // 3. 리더 권한 확인
        if (teamMember.getRole() != UserRole.LEADER) {
            throw new ReportAccessDeniedException();
        }

        return teamMember;
    }

    private ReportResponse convertToResponse(TeamWeeklyReport report) {
        return ReportResponse.builder()
                .id(report.getId())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .report(JsonConverter.parseStringToObject(report.getReport()))
                .teamId(report.getTeam().getId())
                .build();
    }
}
