package com.yaxim.graph;

import com.yaxim.graph.controller.dto.*;
import com.yaxim.graph.exception.*;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamsAnalyticsService {

    private final GraphApiService graphApiService;  // ✅ GraphApiService 주입
    private final UserRepository userRepository;
    private final TeamMemberRepository teamMemberRepository;

    public TeamsUserActivityCountsResponse getTeamsUserActivityCounts(Long userId) {
        String period = "D7";
        TeamsUserActivityCountsResponse response = graphApiService.getTeamsUserActivityCounts(userId, period);

        if (response == null) {
            throw new UserActivityCountsApiCallFailedException();
        }

        return response;
    }

    public TeamsUserActivityUserDetailResponse getTeamsUserActivityUserDetail(Long userId) {
        String period = "D7";
        TeamsUserActivityUserDetailResponse response = graphApiService.getTeamsUserActivityUserDetail(userId, period);

        if (response == null) {
            throw new UserActivityUserDetailApiCallFailedException();
        }

        return response;
    }

    public TeamsTeamActivityDetailResponse getTeamsTeamActivityDetail(Long userId) {
        String period = "D7";
        TeamsTeamActivityDetailResponse response = graphApiService.getTeamsTeamActivityDetail(userId, period);

        if (response == null) {
            throw new TeamActivityDetailApiCallFailed();
        }

        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        String teamId = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new)
                .getTeam().getId();

        List<TeamsTeamActivityDetailResponse.TeamActivity> filteredTeamActivities =
                response.getValue().stream()
                        .filter(activity -> teamId.equals(activity.getTeamId()))
                        .collect(Collectors.toList());

        return TeamsTeamActivityDetailResponse.builder()
                .odataContext(response.getOdataContext())
                .value(filteredTeamActivities)
                .build();
    }

    public TeamsTeamActivityCountsResponse getTeamsTeamActivityCounts(Long userId) {
        String period = "D7";
        TeamsTeamActivityCountsResponse response = graphApiService.getTeamsTeamActivityCounts(userId, period);

        if (response == null) {
            throw new TeamActivityCountsApiCallFailed();
        }

        return response;
    }

    public PersonalDashboardResponse getPersonalDashboard(Long userId) {
        TeamsUserActivityUserDetailResponse data = getTeamsUserActivityUserDetail(userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // ✅ 3. 팀 소속 사용자 리스트 가져오기
        TeamMember teamMembers = teamMemberRepository.findByEmail(user.getEmail())
                .orElseThrow(TeamMemberNotMappedException::new);

        // ✅ 5. API 결과에서 팀 소속 사용자만 필터링
        TeamsUserActivityUserDetailResponse.UserActivity userDetailData = data.getValue().stream()
                .filter(activity -> teamMembers.getEmail().equals(activity.getUserPrincipalName()))
                .findFirst()
                .orElseThrow(UserActivityUserDetailApiCallFailedException::new);

//        TeamsUserActivityCountsResponse userCountsData = getTeamsUserActivityCounts(userId);

        // 객체에서 직접 데이터 추출
        PersonalDashboardResponse.PersonalMetrics personalMetrics = extractPersonalMetrics(userDetailData);
//        List<PersonalDashboardResponse.PersonalWeeklyData> weeklyChart = extractWeeklyChart(userCountsData);

        return PersonalDashboardResponse.builder()
                .personalMetrics(personalMetrics)
//                .weeklyChart(weeklyChart)
                .build();
    }

    private PersonalDashboardResponse.PersonalMetrics extractPersonalMetrics(TeamsUserActivityUserDetailResponse.UserActivity user) {
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

//    private List<PersonalDashboardResponse.PersonalWeeklyData> extractWeeklyChart(TeamsUserActivityCountsResponse userCountsData) {
//        List<PersonalDashboardResponse.PersonalWeeklyData> weeklyData = new ArrayList<>();
//
//        if (userCountsData.getValue() != null) {
//            for (TeamsUserActivityCountsResponse.UserActivityCount dayData : userCountsData.getValue()) {
//                String reportDate = dayData.getReportDate();
//
//                Integer dailyTotal = dayData.getTeamChatMessages() + dayData.getPrivateChatMessages() + dayData.getPostMessages()
//                        + dayData.getPostMessages() + dayData.getCalls();
//
//                PersonalDashboardResponse.PersonalWeeklyData dailyData =
//                        PersonalDashboardResponse.PersonalWeeklyData.builder()
//                                .date(reportDate.substring(5))
//                                .teamChats(dayData.getTeamChatMessages())
//                                .privateChats(dayData.getPrivateChatMessages())
//                                .posts(dayData.getPostMessages())
//                                .meetings(dayData.getMeetings())
//                                .calls(dayData.getCalls())
//                                .dailyTotal(dailyTotal)
//                                .build();
//
//                weeklyData.add(dailyData);
//            }
//        }
//
//        return weeklyData;
//    }

    public TeamDashboardResponse getTeamDashboard(Long userId) {
        log.info("팀 대시보드 데이터 조회: userId: {}", userId);
        TeamsTeamActivityDetailResponse teamDetailData = getTeamsTeamActivityDetail(userId);
        TeamsTeamActivityCountsResponse teamCountsData = getTeamsTeamActivityCounts(userId);

        // 객체에서 직접 데이터 추출
        TeamDashboardResponse.TeamMetrics teamMetrics = extractTeamMetrics(teamDetailData);
        List<TeamDashboardResponse.TeamWeeklyData> weeklyChart = extractTeamWeeklyChart(teamCountsData);

        return TeamDashboardResponse.builder()
                .teamMetrics(teamMetrics)
                .weeklyChart(weeklyChart)
                .build();
    }

    private TeamDashboardResponse.TeamMetrics extractTeamMetrics(TeamsTeamActivityDetailResponse teamDetailData) {
        TeamsTeamActivityDetailResponse.TeamActivity selectedTeam = teamDetailData.getValue().get(0);

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