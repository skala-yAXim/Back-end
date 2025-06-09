package com.yaxim.dashboard.service;

import com.yaxim.dashboard.controller.dto.response.*;
import com.yaxim.dashboard.exception.GraphApiCallFailedException;
import com.yaxim.dashboard.exception.TeamsAnalyticsNotFoundException;
import com.yaxim.global.graph.GraphApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamsAnalyticsService {

    private final GraphApiService graphApiService;  // ✅ GraphApiService 주입

    public TeamsUserActivityCountsResponse getTeamsUserActivityCounts(Long userId) {
        try {
            log.info("TeamsUserActivityCounts 데이터 조회: userId: {}", userId);

            String period = "D7";
            TeamsUserActivityCountsResponse response = graphApiService.getTeamsUserActivityCounts(userId, period);

            if (response == null) {
                throw new TeamsAnalyticsNotFoundException();
            }

            return response;

        } catch (Exception e) {
            log.error("TeamsUserActivityCounts 데이터 조회 실패", e);
            throw new GraphApiCallFailedException();
        }
    }

    public TeamsUserActivityUserDetailResponse getTeamsUserActivityUserDetail(Long userId) {
        try {
            log.info("TeamsUserActivityUserDetail 데이터 조회: userId: {}", userId);

            String period = "D7";
            TeamsUserActivityUserDetailResponse response = graphApiService.getTeamsUserActivityUserDetail(userId, period);

            if (response == null) {
                throw new TeamsAnalyticsNotFoundException();
            }

            return response;

        } catch (Exception e) {
            log.error("TeamsUserActivityUserDetail 데이터 조회 실패", e);
            throw new GraphApiCallFailedException();
        }
    }
    public TeamsTeamActivityDetailResponse getTeamsTeamActivityDetail(Long userId) {
        try {
            log.info("TeamsTeamActivityDetail 조회: userID: {}", userId);

            String period = "D7";
            TeamsTeamActivityDetailResponse response = graphApiService.getTeamsTeamActivityDetail(userId, period);

            if (response == null) {
                throw new TeamsAnalyticsNotFoundException();
            }

            return response;

        } catch (Exception e) {
            log.error("TeamsTeamActivityDetail 데이터 조회 실패", e);
            throw new GraphApiCallFailedException();
        }
    }
    public TeamsTeamActivityCountsResponse getTeamsTeamActivityCounts(Long userId) {
        try {
            log.info("TeamsTeamActivityCounts 조회: userID: {}", userId);

            String period = "D7";
            TeamsTeamActivityCountsResponse response = graphApiService.getTeamsTeamActivityCounts(userId, period);

            if (response == null) {
                throw new TeamsAnalyticsNotFoundException();
            }

            return response;

        } catch (Exception e) {
            log.error("TeamsTeamActivityCounts 데이터 조회 실패", e);
            throw new GraphApiCallFailedException();
        }
    }

    public PersonalDashboardResponse getPersonalDashboard(Long userId) {
        try {
            log.info("개인 대시보드 데이터 조회: userId: {}", userId);

            // 객체 직접 받기
            TeamsUserActivityUserDetailResponse userDetailData = graphApiService.getTeamsUserActivityUserDetail(userId, "D7");
            TeamsUserActivityCountsResponse userCountsData = graphApiService.getTeamsUserActivityCounts(userId, "D7");

            if (userDetailData == null || userCountsData == null) {
                throw new TeamsAnalyticsNotFoundException();
            }

            // 객체에서 직접 데이터 추출
            PersonalDashboardResponse.PersonalMetrics personalMetrics = extractPersonalMetrics(userDetailData);
            List<PersonalDashboardResponse.PersonalWeeklyData> weeklyChart = extractWeeklyChart(userCountsData);

            return PersonalDashboardResponse.builder()
                    .personalMetrics(personalMetrics)
                    .weeklyChart(weeklyChart)
                    .build();

        } catch (Exception e) {
            log.error("개인 대시보드 데이터 조회 실패 - 에러 타입: {}, 메시지: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new GraphApiCallFailedException();
        }
    }

    private PersonalDashboardResponse.PersonalMetrics extractPersonalMetrics(TeamsUserActivityUserDetailResponse userDetailData) {
        TeamsUserActivityUserDetailResponse.UserActivity user = userDetailData.getValue().get(0);

        Integer totalActivity = user.getTeamChatMessageCount() + user.getPrivateChatMessageCount()
                + user.getPostMessages() + user.getMeetingCount() + user.getCallCount();

        return PersonalDashboardResponse.PersonalMetrics.builder()
                .teamChatCount(user.getTeamChatMessageCount())
                .privateChatCount(user.getPrivateChatMessageCount())
                .postCount(user.getPostMessages())
                .meetingCount(user.getMeetingCount())
                .callCount(user.getCallCount())
                .urgentMessages(user.getUrgentMessages())
                .totalActivity(totalActivity)
                .build();
    }

    private List<PersonalDashboardResponse.PersonalWeeklyData> extractWeeklyChart(TeamsUserActivityCountsResponse userCountsData) {
        List<PersonalDashboardResponse.PersonalWeeklyData> weeklyData = new ArrayList<>();

        if (userCountsData.getValue() != null) {
            for (TeamsUserActivityCountsResponse.UserActivityCount dayData : userCountsData.getValue()) {
                String reportDate = dayData.getReportDate();

                Integer dailyTotal = dayData.getTeamChatMessages() + dayData.getPrivateChatMessages() + dayData.getPostMessages()
                        + dayData.getPostMessages() + dayData.getCalls();

                PersonalDashboardResponse.PersonalWeeklyData dailyData =
                        PersonalDashboardResponse.PersonalWeeklyData.builder()
                                .date(reportDate.substring(5))
                                .teamChats(dayData.getTeamChatMessages())
                                .privateChats(dayData.getPrivateChatMessages())
                                .posts(dayData.getPostMessages())
                                .meetings(dayData.getMeetings())
                                .calls(dayData.getCalls())
                                .dailyTotal(dailyTotal)
                                .build();

                weeklyData.add(dailyData);
            }
        }

        return weeklyData;
    }
    public TeamDashboardResponse getTeamDashboard(Long userId) {
        try {
            log.info("팀 대시보드 데이터 조회: userId: {}", userId);
            TeamsTeamActivityDetailResponse teamDetailData = graphApiService.getTeamsTeamActivityDetail(userId, "D7");
            TeamsTeamActivityCountsResponse teamCountsData = graphApiService.getTeamsTeamActivityCounts(userId, "D7");

            if (teamDetailData == null || teamCountsData == null) {
                throw new TeamsAnalyticsNotFoundException();
            }

            // 객체에서 직접 데이터 추출
            TeamDashboardResponse.TeamMetrics teamMetrics = extractTeamMetrics(teamDetailData);
            List<TeamDashboardResponse.TeamWeeklyData> weeklyChart = extractTeamWeeklyChart(teamCountsData);

            return TeamDashboardResponse.builder()
                    .teamMetrics(teamMetrics)
                    .weeklyChart(weeklyChart)
                    .build();

        } catch (Exception e) {
            log.error("팀 대시보드 데이터 조회 실패 - 에러 타입: {}, 메시지: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw new GraphApiCallFailedException();
        }
    }

    private TeamDashboardResponse.TeamMetrics extractTeamMetrics(TeamsTeamActivityDetailResponse teamDetailData) {
        TeamsTeamActivityDetailResponse.TeamActivity selectedTeam = findTeamWithName(teamDetailData);

        if (selectedTeam == null && teamDetailData.getValue() != null && !teamDetailData.getValue().isEmpty()) {
            // teamName이 있는 팀이 없으면 첫 번째 팀 사용
            selectedTeam = teamDetailData.getValue().get(0);
        }

        if (selectedTeam == null || selectedTeam.getDetails() == null || selectedTeam.getDetails().isEmpty()) {
            throw new TeamsAnalyticsNotFoundException();
        }

        TeamsTeamActivityDetailResponse.ActivityDetail teamDetails = selectedTeam.getDetails().get(0);

        return TeamDashboardResponse.TeamMetrics.builder()
                .teamName(selectedTeam.getTeamName() != null ? selectedTeam.getTeamName() : "익명팀")
                .teamType(selectedTeam.getTeamType() != null ? selectedTeam.getTeamType() : "Private")
                .activeUsers(teamDetails.getActiveUsers())
                .activeChannels(teamDetails.getActiveChannels())
                .channelMessages(teamDetails.getChannelMessages())
                .postMessages(teamDetails.getPostMessages())
                .reactions(teamDetails.getReactions())
                .mentions(teamDetails.getMentions())
                .lastActivityDate(selectedTeam.getLastActivityDate() != null ? selectedTeam.getLastActivityDate() : "")
                .build();
    }

    private List<TeamDashboardResponse.TeamWeeklyData> extractTeamWeeklyChart(TeamsTeamActivityCountsResponse teamCountsData) {
        List<TeamDashboardResponse.TeamWeeklyData> weeklyData = new ArrayList<>();

        if (teamCountsData.getValue() != null) {
            for (TeamsTeamActivityCountsResponse.TeamReport dayData : teamCountsData.getValue()) {
                if (dayData.getUserCounts() != null) {
                    for (TeamsTeamActivityCountsResponse.UserCount dailyUserData : dayData.getUserCounts()) {
                        String reportDate = dailyUserData.getReportDate();

                        Integer dailyTotal = dailyUserData.getPostMessages() + dailyUserData.getPostMessages()
                                + dailyUserData.getMeetingsOrganized() + dailyUserData.getReactions() + dailyUserData.getMentions()
                                + dailyUserData.getUrgentMessages();

                        TeamDashboardResponse.TeamWeeklyData dailyData =
                                TeamDashboardResponse.TeamWeeklyData.builder()
                                        .date(reportDate.substring(5))
                                        .activeUsers(dailyUserData.getActiveUsers())
                                        .channelMessages(dailyUserData.getPostMessages())
                                        .postMessages(dailyUserData.getChannelMessages())
                                        .meetingsOrganized(dailyUserData.getMeetingsOrganized())
                                        .reactions(dailyUserData.getReactions())
                                        .mentions(dailyUserData.getMentions())
                                        .urgentMessages(dailyUserData.getUrgentMessages())
                                        .dailyTotal(dailyTotal)
                                        .build();

                        weeklyData.add(dailyData);
                    }
                }
            }
        }

        return weeklyData;
    }

    private TeamsTeamActivityDetailResponse.TeamActivity findTeamWithName(TeamsTeamActivityDetailResponse teamDetailData) {
        if (teamDetailData.getValue() != null) {
            for (TeamsTeamActivityDetailResponse.TeamActivity team : teamDetailData.getValue()) {
                String teamName = team.getTeamName();

                // teamName이 null이 아니고 빈 문자열이 아니면서 "null"이 아닌 경우
                if (teamName != null && !teamName.isEmpty() && !"null".equals(teamName)) {
                    log.info("teamName이 존재하는 팀 발견: {}", teamName);
                    return team;
                }
            }
        }

        log.warn("teamName이 존재하는 팀을 찾을 수 없음. 첫 번째 팀 사용.");
        return null;
    }
}