package com.yaxim.statics.service;

import com.yaxim.statics.controller.dto.response.AverageStaticsResponse;
import com.yaxim.statics.controller.dto.response.GeneralStaticsResponse;
import com.yaxim.statics.entity.Weekday;
import com.yaxim.statics.entity.DailyTeamActivity;
import com.yaxim.statics.entity.select.AverageActivity;
import com.yaxim.statics.entity.select.TeamActivity;
import com.yaxim.statics.repository.TeamStaticsRepository;
import com.yaxim.statics.repository.UserStaticsRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.repository.TeamRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamStaticsService {
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserStaticsRepository userStaticsRepository;
    private final TeamStaticsRepository teamStaticsRepository;
    private final TeamRepository teamRepository;

    public List<GeneralStaticsResponse> getTeamStatic(Long userId) {
        Team team = getTeamByUserId(userId);

        Boolean hasData = teamStaticsRepository.existsAllByTeamId(team.getId());

        List<DailyTeamActivity> activities = new ArrayList<>();

        if (hasData) {
            activities = teamStaticsRepository.findAllByTeam(team);
        } else {
            List<Users> users = userRepository.findAllByEmailIn(
                    teamMemberRepository.getEmailsByTeamIn(team)
            );

            for (int i = 0; i < 7; i++) {
                TeamActivity data = userStaticsRepository.getTeamActivityByWeekdayAndUser(Weekday.of(i), users);

                DailyTeamActivity activity = teamStaticsRepository.save(
                        new DailyTeamActivity(
                                data.getReportDate(),
                                data.getTeamsPost(),
                                data.getEmailSend(),
                                data.getEmailReceive(),
                                data.getDocsDocx(),
                                data.getDocsXlsx(),
                                data.getDocsTxt(),
                                data.getDocsEtc(),
                                data.getGitPullRequest(),
                                data.getGitCommit(),
                                data.getGitIssue(),
                                team,
                                Weekday.of(i)
                        )
                );

                activities.add(activity);
            }
        }

        return activities.stream()
                .map(GeneralStaticsResponse::from)
                .toList();
    }

    public List<AverageStaticsResponse> getTeamsAverageStatic() {
        List<Team> teams = teamRepository.findAll();

        List<AverageActivity> activities = new ArrayList<>();

        for (Team team : teams) {
            for (int i = 0; i < 7; i++) {
                activities.add(teamStaticsRepository.getTeamAvgByDayAndTeam(Weekday.of(i), team));
            }
        }

        return activities.stream()
                .map(AverageStaticsResponse::from)
                .toList();
    }

    private Team getTeamByUserId(Long userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam();
    }
}
