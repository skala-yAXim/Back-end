package com.yaxim.report.service;

import com.yaxim.dashboard.comment.service.CommentService;
import com.yaxim.global.for_ai.dto.request.TeamWeeklyReportCreateRequest;
import com.yaxim.project.service.ProjectService;
import com.yaxim.report.controller.dto.request.TeamMemberWeeklyPageRequest;
import com.yaxim.report.controller.dto.response.*;
import com.yaxim.report.entity.TeamWeeklyReport;
import com.yaxim.report.entity.UserWeeklyReport;
import com.yaxim.report.exception.ReportAccessDeniedException;
import com.yaxim.report.exception.ReportNotFoundException;
import com.yaxim.report.repository.TeamMemberWeeklyPageRepository;
import com.yaxim.report.repository.TeamWeeklyReportRepository;
import com.yaxim.report.repository.UserWeeklyReportRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Spring의 Transactional import

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamWeeklyReportService {

    private final TeamWeeklyReportRepository teamWeeklyReportRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;
    private final UserWeeklyReportRepository userWeeklyReportRepository;
    private final TeamMemberWeeklyPageRepository teamMemberWeeklyPageRepository;
    private final CommentService commentService;
    private final ProjectService projectService;

    @Transactional
    public TeamWeeklyReportResponse createTeamWeeklyReport(TeamWeeklyReportCreateRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(TeamMemberNotMappedException::new);

        TeamWeeklyReport report = TeamWeeklyReport.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .report(request.getReport())
                .team(team)
                .build();

        teamWeeklyReportRepository.save(report);

        commentService.addComment(
                team,
                (String) report.getReport()
                        .getOrDefault("weekly_short_review", "")
        );

        projectService.updateProjectProgress(
                (List) report.getReport()
                        .getOrDefault("team_weekly_report", new ArrayList<>())
        );

        return TeamWeeklyReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public Page<TeamWeeklyReportResponse> getTeamWeeklyReport(Long userId, Pageable pageable) {
        // 검증 로직을 헬퍼 메서드로 통합
        TeamMember viewer = validateUserAndGetTeamMember(userId);

        return teamWeeklyReportRepository.findByTeam(
                        viewer.getTeam(),
                        pageable
                )
                .map(TeamWeeklyReportResponse::from);
    }

    @Transactional(readOnly = true)
    public TeamWeeklyDetailResponse getReportById(Long reportId, Long userId) {
        // 1. 요청자 정보 확인
        TeamMember viewer = validateUserAndGetTeamMember(userId);

        // 2. 보고서 조회
        TeamWeeklyReport report = teamWeeklyReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        // 3. 요청자가 해당 보고서의 팀에 속한 리더인지 확인
        if (!report.getTeam().getId().equals(viewer.getTeam().getId())) {
            throw new ReportAccessDeniedException();
        }

        return TeamWeeklyDetailResponse.from(report);
    }

    @Transactional
    public void deleteReport(Long reportId, Long userId) {
        // 1. 요청자 정보 확인
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

    @Transactional(readOnly = true)
    public Page<TeamMemberWeeklyReportResponse> getTeamMemberWeeklyReports(
            TeamMemberWeeklyPageRequest request,
            Pageable pageable,
            Long userId
    ) {
        // 1. 요청자 정보 확인
        TeamMember viewer = validateUserAndGetTeamMember(userId);

        // 2. 요청자의 팀에 속한 멤버들의 Weekly 조회
        Page<UserWeeklyReport> reports = teamMemberWeeklyPageRepository.findTeamMemberWeekly(
                request,
                viewer.getTeam(),
                pageable
        );

        return reports.map(TeamMemberWeeklyReportResponse::fromTeam);
    }

    @Transactional(readOnly = true)
    public TeamMemberWeeklyDetailResponse getTeamMemberWeeklyReport(Long reportId, Long userId) {
        // 1. 요청자 정보 확인
        TeamMember viewer = validateUserAndGetTeamMember(userId);

        // 2. reportId로 Weekly 조회
        UserWeeklyReport report = userWeeklyReportRepository.findById(reportId)
                .orElseThrow(ReportNotFoundException::new);

        // 3. 요청자의 팀 정보와 reportId로 조회한 보고서의 팀 정보가 일치하는지 확인
        if (!report.getTeam().getId().equals(viewer.getTeam().getId()))
            throw new ReportAccessDeniedException();

        return TeamMemberWeeklyDetailResponse.from(report);
    }

    /**
     * 사용자의 팀을 불러와 TeamMember 객체를 반환하는 헬퍼 메서드
     * @param userId 사용자의 ID
     * @return TeamMember 객체
     */
    private TeamMember validateUserAndGetTeamMember(Long userId) {
        // 사용자의 이메일로 팀 멤버 정보 조회 (팀 소속 여부 확인)
        return teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new);
    }
}
