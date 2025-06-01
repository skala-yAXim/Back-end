package com.yaxim.team.service;

import com.yaxim.global.auth.jwt.TokenService;
import com.yaxim.global.auth.oauth2.exception.OidcTokenExpiredException;
import com.yaxim.global.graph.GraphApiService;
import com.yaxim.global.graph.GraphTeamMemberResponse;
import com.yaxim.global.graph.GraphTeamResponse;
import com.yaxim.team.controller.dto.response.TeamResponse;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
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

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final GraphApiService graphApiService;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public List<TeamResponse> getUserTeams(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return getTeamResponse(
//                teamMemberRepository.findAllByUserId(userId)
                teamMemberRepository.findAllByEmail(user.getEmail())
                        .stream()
                        .map(m ->
                                teamRepository.findById(m.getTeam().getId())
                                        .orElseThrow(TeamNotFoundException::new)
                        ).toList()
        );
    }

    @Transactional
    public TeamResponse loadOneTeam(Long userId, String teamId) {
        GraphTeamResponse.Team graphTeam = graphApiService.getTeamInfo(userId, teamId);
        List<GraphTeamMemberResponse.Members> graphTeamMembers = graphApiService.getMyTeamMembers(teamId, userId);

        Team team = teamRepository.save(
                new Team(
                        graphTeam.id,
                        graphTeam.displayName,
                        graphTeam.description
                )
        );

//        Users user = userRepository.findById(userId)
//                        .orElseThrow(UserNotFoundException::new);

        graphTeamMembers
                .forEach(m -> {
                    UserRole role = m.roles.isEmpty() ? UserRole.MEMBER : UserRole.LEADER;

                    teamMemberRepository.save(
                            new TeamMember(
                                    team,
                                    m.email,
                                    role
                            )
                    );
        });

        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getDescription()
        );
    }

    @Transactional
    public List<TeamResponse> loadAllTeams(Long userId) {
        List<GraphTeamResponse.Team> graphTeams = graphApiService.getMyTeams(userId);

        List<Team> teams = graphTeams.stream()
                .map(t -> teamRepository.save(
                        new Team(
                                t.id,
                                t.displayName,
                                t.description
                        )
                )).toList();

        teams.forEach(t -> {
            List<GraphTeamMemberResponse.Members> graphTeamMembers = graphApiService.getMyTeamMembers(t.getId(), userId);

            graphTeamMembers.forEach(m -> {
                UserRole role = m.roles.isEmpty() ? UserRole.MEMBER : UserRole.LEADER;

                teamMemberRepository.save(
                        new TeamMember(
                                t,
                                m.email,
                                role
                        )
                );
            });
        });

        return getTeamResponse(teams);
    }

    private List<TeamResponse> getTeamResponse(List<Team> teams) {
        return teams.stream()
                .map(t -> new TeamResponse(
                        t.getId(),
                        t.getName(),
                        t.getDescription()
                )).toList();
    }
}
