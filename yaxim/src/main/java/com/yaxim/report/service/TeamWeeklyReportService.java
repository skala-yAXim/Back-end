package com.yaxim.report.service;

import com.yaxim.global.for_ai.dto.request.WeeklyReportCreateRequest;
import com.yaxim.report.controller.dto.response.WeeklyReportDetailResponse;
import com.yaxim.report.controller.dto.response.WeeklyReportResponse;
import com.yaxim.report.entity.TeamWeeklyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.TeamWeeklyReportRepository;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional import

@Service
@RequiredArgsConstructor
public class TeamWeeklyReportService {

    private final TeamWeeklyReportRepository teamWeeklyReportRepository;
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public WeeklyReportDetailResponse createTeamWeeklyReport(Long userId, WeeklyReportCreateRequest request) {
        TeamMember creator = validateUserAndGetTeamMember(userId);

        TeamWeeklyReport report = TeamWeeklyReport.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .report(request.getReport())
                .team(creator.getTeam())
                .build();

        teamWeeklyReportRepository.save(report);
        return WeeklyReportDetailResponse.from(report);
    }

    @Transactional(readOnly = true)
    public Page<WeeklyReportResponse> getTeamWeeklyReports(Long userId, Pageable pageable) {
        // 검증 로직을 헬퍼 메서드로 통합
        TeamMember viewer = validateUserAndGetTeamMember(userId);

        return teamWeeklyReportRepository.findByTeam(
                        viewer.getTeam(),
                        pageable
                )
                .map(WeeklyReportResponse::fromTeam);
    }

    @Transactional(readOnly = true)
    public WeeklyReportDetailResponse getReportById(Long reportId, Long userId) {
        // 1. 요청자의 팀 정보 확인
        TeamMember viewer = validateUserAndGetTeamMember(userId);

        // 2. 보고서 조회
        TeamWeeklyReport report = teamWeeklyReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        // 3. 요청자가 해당 보고서의 팀에 속한 리더인지 확인
        if (!report.getTeam().getId().equals(viewer.getTeam().getId())) {
            throw new ReportAccessDeniedException();
        }

        return WeeklyReportDetailResponse.from(report);
    }

    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        // 1. 요청자의 팀 정보 확인
        TeamMember deleter = validateUserAndGetTeamMember(userId);

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
     * 사용자의 팀을 불러와 TeamMember 객체를 반환하는 헬퍼 메서드
     * @param userId 사용자의 ID
     * @return TeamMember 객체
     */
    private TeamMember validateUserAndGetTeamMember(Long userId) {
        // 1. 사용자 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 2. 사용자의 이메일로 팀 멤버 정보 조회 (팀 소속 여부 확인)
        return teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new);
    }
}
