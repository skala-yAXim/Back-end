package com.yaxim.team.service;

import com.yaxim.graph.GraphApiService;
import com.yaxim.graph.controller.dto.GraphTeamMemberResponse;
import com.yaxim.graph.controller.dto.GraphTeamResponse;
import com.yaxim.project.controller.dto.response.ProjectDetailResponse;
import com.yaxim.project.entity.Project;
import com.yaxim.project.repository.ProjectCustomRepository;
import com.yaxim.team.controller.dto.request.WeeklyTemplateRequest;
import com.yaxim.team.controller.dto.response.TeamMemberResponse;
import com.yaxim.team.controller.dto.response.TeamResponse;
import com.yaxim.global.for_ai.dto.response.TeamWithMemberAndProjectResponse;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.exception.TeamNotFoundException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.repository.TeamRepository;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final GraphApiService graphApiService;
    private final UserRepository userRepository;
    private final ProjectCustomRepository projectCustomRepository;

    public TeamResponse getUserTeam(Long userId) {
        String teamId = teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam().getId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        return TeamResponse.from(team);
    }

    @Transactional
    public void updateTemplate(
            WeeklyTemplateRequest request,
            Long userId
    ) {
        Team team = teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam();

        team.setWeeklyTemplate(
                request.getTemplate()
        );
    }

    public List<TeamMemberResponse> getUserTeamMembers(Long userId) {
        String teamId = teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam().getId();

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);

        return getTeamMemberResponse(members);
    }

    @Transactional
    public void loadTeam(Long userId) {
        GraphTeamResponse.Team graphTeam = graphApiService.getMyFirstTeam(userId);

        if (graphTeam == null) {
            throw new TeamNotFoundException();
        }

        Team team = teamRepository.findById(graphTeam.id)
                .orElseGet(() -> teamRepository.save(new Team(
                        graphTeam.id,
                        graphTeam.displayName,
                        graphTeam.description
                )));

        team.setUpdatedAt(LocalDateTime.now());

        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        List<GraphTeamMemberResponse.Members> graphTeamMembers = graphApiService.getMyTeamMembers(
                graphTeam.id,
                userId
        );

        UserRole role = graphTeamMembers.stream().filter(m -> m.email.equals(user.getEmail()))
                .findFirst()
                .filter(m -> !m.roles.isEmpty())
                .map(m -> UserRole.LEADER).orElse(UserRole.MEMBER);

        TeamMember member = teamMemberRepository.findByUserId(userId)
                .orElseGet(() -> teamMemberRepository.save(new TeamMember(
                        team,
                        user
                )));

        member.updateRole(role);
    }

    private List<TeamMemberResponse> getTeamMemberResponse(List<TeamMember> members) {
        return members.stream()
                .map(m -> {
                    Users user = m.getUser();

                    return new TeamMemberResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail()
                    );
                }).toList();
    }

    public List<TeamWithMemberAndProjectResponse> getAllTeamsInfo() {
        List<Team> teams = teamRepository.findAll();

        return teams.stream()
                .map(team -> {
                    List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
                    List<TeamMemberResponse> memberResponses = getTeamMemberResponse(members);
                    List<Project> projects = projectCustomRepository.findAllInProgressByTeam(team);

                    return new TeamWithMemberAndProjectResponse(
                            team.getId(),
                            team.getName(),
                            team.getDescription(),
                            team.getWeeklyTemplate(),
                            memberResponses,
                            projects.stream().map(ProjectDetailResponse::from).toList()
                    );
                }).toList();
    }
}