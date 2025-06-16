package com.yaxim.team.service;

import com.yaxim.graph.GraphApiService;
import com.yaxim.graph.controller.dto.GraphTeamMemberResponse;
import com.yaxim.graph.controller.dto.GraphTeamResponse;
import com.yaxim.project.controller.dto.response.ProjectDetailResponse;
import com.yaxim.project.entity.Project;
import com.yaxim.project.repository.ProjectRepository;
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
    private final ProjectRepository projectRepository;

    public TeamResponse getUserTeam(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String teamId = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam().getId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(TeamNotFoundException::new);

        return getTeamResponse(team);
    }

    public List<TeamMemberResponse> getUserTeamMembers(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String teamId = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam().getId();

        List<TeamMember> members = teamMemberRepository.findByTeamId(teamId);

        return getTeamMemberResponse(members);
    }

    @Transactional
    public TeamResponse loadTeam(Long userId) {
        GraphTeamResponse.Team graphTeam = graphApiService.getMyFirstTeam(userId);

        if (graphTeam == null) {
            throw new TeamNotFoundException();
        }

        List<GraphTeamMemberResponse.Members> graphTeamMembers = graphApiService.getMyTeamMembers(
                graphTeam.id,
                userId
        );

        boolean isNewTeam = !teamRepository.existsById(graphTeam.id);

        Team team = teamRepository.findById(graphTeam.id)
                .orElseGet(() -> teamRepository.save(new Team(
                        graphTeam.id,
                        graphTeam.displayName,
                        graphTeam.description
                )));

        team.setUpdatedAt(LocalDateTime.now());

        if (isNewTeam) {
            for (GraphTeamMemberResponse.Members m : graphTeamMembers) {
                UserRole role = m.roles.isEmpty() ? UserRole.MEMBER : UserRole.LEADER;
                teamMemberRepository.save(new TeamMember(team, m.getEmail(), role));
            }
        } else {
            team.syncMembers(graphTeamMembers, teamMemberRepository);
        }

        return getTeamResponse(team);
    }

    private TeamResponse getTeamResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getCreatedAt(),
                team.getUpdatedAt(),
                team.getName(),
                team.getDescription()
        );
    }

    private List<TeamMemberResponse> getTeamMemberResponse(List<TeamMember> members) {
        return members.stream()
                .map(m -> {
                    Users user = userRepository.findByEmail(m.getEmail())
//                            .orElseThrow(UserNotFoundException::new);
                            .orElseGet(() ->
                                    new Users(
                                            0,
                                            "아직 가입하지 않은 사용자입니다.",
                                            m.getEmail()));

                    return new TeamMemberResponse(
                            user.getId(),
                            user.getName(),
                            m.getEmail()
                    );
                }).toList();
    }

    public List<TeamWithMemberAndProjectResponse> getAllTeamsInfo() {
        List<Team> teams = teamRepository.findAll();

        return teams.stream()
                .map(team -> {
                    List<TeamMember> members = teamMemberRepository.findByTeamId(team.getId());
                    List<TeamMemberResponse> memberResponses = getTeamMemberResponse(members);
                    List<Project> projects = projectRepository.findByTeam(team);

                    return new TeamWithMemberAndProjectResponse(
                            team.getId(),
                            team.getName(),
                            team.getDescription(),
                            memberResponses,
                            projects.stream().map(ProjectDetailResponse::from).toList()
                    );
                }).toList();
    }
}