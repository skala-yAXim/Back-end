package com.yaxim.dashboard.statics.service;

import com.yaxim.dashboard.statics.controller.dto.response.AverageStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.GeneralStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.SumStaticResponse;
import com.yaxim.dashboard.statics.entity.Weekday;
import com.yaxim.dashboard.statics.entity.DailyTeamActivity;
import com.yaxim.dashboard.statics.entity.select.AverageActivity;
import com.yaxim.dashboard.statics.entity.select.TeamActivity;
import com.yaxim.dashboard.statics.repository.TeamStaticsRepository;
import com.yaxim.dashboard.statics.repository.UserStaticsRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.repository.TeamRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamStaticsService {
    private final TeamMemberRepository teamMemberRepository;
    private final UserStaticsRepository userStaticsRepository;
    private final TeamStaticsRepository teamStaticsRepository;
    private final TeamRepository teamRepository;

    public List<GeneralStaticsResponse> getTeamStatic(Long userId) {
        Team team = getTeamByUserId(userId);

        Boolean hasData = teamStaticsRepository.existsAllByTeamId(team.getId());

        List<DailyTeamActivity> activities;

        if (hasData) {
            activities = teamStaticsRepository.findAllByTeam(team);
        } else {
            activities = createTeamActivity(team);
        }

        return activities.stream()
                .map(GeneralStaticsResponse::from)
                .toList();
    }

    public List<AverageStaticsResponse> getTeamsAverageStatic() {
        List<Team> teams = teamRepository.findAll();

        List<AverageActivity> activities = new ArrayList<>();

        for (Team team : teams) {
            if (!teamStaticsRepository.existsAllByTeamId(team.getId())) {
                if (createTeamActivity(team).isEmpty()) {
                    continue;
                }
            }

            for (Weekday i : Weekday.values()) {
                teamStaticsRepository.getTeamAvgByDayAndTeam(i, team)
                        .ifPresent(activities::add);
            }
        }

        return activities.stream()
                .map(AverageStaticsResponse::from)
                .toList();
    }

    public SumStaticResponse getTeamWeekStatics(Long userId) {
        Team team = getTeamByUserId(userId);

        Boolean hasData = teamStaticsRepository.existsAllByTeamId(team.getId());

        if (!hasData) {
            List<GeneralStaticsResponse> created = getTeamStatic(userId);

            if (created.isEmpty()) {
                return new SumStaticResponse();
            }
        }

        return SumStaticResponse.from(
                teamStaticsRepository.getTeamWeekActivity(team)
        );
    }

    private Team getTeamByUserId(Long userId) {
        return teamMemberRepository.findByUserId(userId)
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam();
    }

    private List<DailyTeamActivity> createTeamActivity(Team team) {
        List<DailyTeamActivity> activities = new ArrayList<>();
        List<Users> users = teamMemberRepository.getUsersByTeamIn(team);

        for (Weekday i : Weekday.values()) {
            Optional<TeamActivity> optional = userStaticsRepository.getTeamActivityByWeekdayAndUser(i, users);
            if (optional.isEmpty()) {
                continue;
            }

            TeamActivity data = optional.get();

            DailyTeamActivity activity = teamStaticsRepository.save(
                    new DailyTeamActivity(
                            data.getReportDate(),
                            data.getTeamsPost(),
                            data.getTeamsReply(),
                            data.getEmailSend(),
                            data.getEmailReceive(),
                            data.getDocsDocx(),
                            data.getDocsXlsx(),
                            data.getDocsPptx(),
                            data.getDocsEtc(),
                            data.getGitPullRequest(),
                            data.getGitCommit(),
                            data.getGitIssue(),
                            team,
                            i
                    )
            );

            activities.add(activity);
        }

        return activities;
    }
}
