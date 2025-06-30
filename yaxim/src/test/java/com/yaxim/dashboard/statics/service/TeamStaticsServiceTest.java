package com.yaxim.dashboard.statics.service;

import com.yaxim.dashboard.statics.controller.dto.response.AverageStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.GeneralStaticsResponse;
import com.yaxim.dashboard.statics.controller.dto.response.SumStaticResponse;
import com.yaxim.dashboard.statics.entity.DailyTeamActivity;
import com.yaxim.dashboard.statics.entity.Weekday;
import com.yaxim.dashboard.statics.entity.select.AverageActivity;
import com.yaxim.dashboard.statics.entity.select.SumActivity;
import com.yaxim.dashboard.statics.repository.TeamStaticsRepository;
import com.yaxim.dashboard.statics.repository.UserStaticsRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.repository.TeamRepository;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamStaticsServiceTest {

    @InjectMocks
    private TeamStaticsService teamStaticsService;

    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private UserStaticsRepository userStaticsRepository;
    @Mock
    private TeamStaticsRepository teamStaticsRepository;
    @Mock
    private TeamRepository teamRepository;

    private static final Long USER_ID = 1L;
    private static final String TEAM_ID = "test_team_id";

    private Team team;
    private Users user;
    private TeamMember teamMember;
    private DailyTeamActivity activity;

    @BeforeEach
    void setup() {

        team = new Team(
                TEAM_ID,
                "test",
                "description"
        );

        user = new Users(
                USER_ID,
                "name",
                "email@test.com"
        );

        teamMember = new TeamMember(
                team,
                user,
                UserRole.LEADER
        );

        activity = new DailyTeamActivity(
                team,
                Weekday.MONDAY
        );
    }

    @Test
    @DisplayName("팀 업무 통계 조회")
    void testGetTeamStatic_withExistingData() {
        // given
        when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(teamMember));
        when(teamStaticsRepository.existsAllByTeamId(TEAM_ID))
                .thenReturn(true);
        when(teamStaticsRepository.findAllByTeam(team))
                .thenReturn(List.of(activity));

        // when
        List<GeneralStaticsResponse> result = teamStaticsService.getTeamStatic(USER_ID);

        // then
        assertEquals(1, result.size());
        verify(teamStaticsRepository).findAllByTeam(team);
    }

    @Test
    @DisplayName("팀 단위 전체 평균 통계 조회")
    void testGetTeamsAverageStatic() {
        // given
        when(teamRepository.findAll())
                .thenReturn(List.of(team));
        when(teamStaticsRepository.existsAllByTeamId(TEAM_ID))
                .thenReturn(true);

        for (Weekday day : Weekday.values()) {
            when(teamStaticsRepository.getTeamAvgByDayAndTeam(day, team))
                    .thenReturn(Optional.of(new AverageActivity()));
        }

        // when
        List<AverageStaticsResponse> result = teamStaticsService.getTeamsAverageStatic();

        // then
        assertEquals(7, result.size());
        verify(teamStaticsRepository, times(7)).getTeamAvgByDayAndTeam(any(), eq(team));
    }

    @Test
    @DisplayName("팀 일주일 업무 통계 조회")
    void testGetTeamWeekStatics() {
        // given
        when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(teamMember));
        when(teamStaticsRepository.existsAllByTeamId(TEAM_ID))
                .thenReturn(true);
        when(teamStaticsRepository.getTeamWeekActivity(team))
                .thenReturn(new SumActivity());

        // when
        SumStaticResponse result = teamStaticsService.getTeamWeekStatics(USER_ID);

        // then
        assertNotNull(result);
        verify(teamStaticsRepository).getTeamWeekActivity(team);
    }

    @Test
    @DisplayName("일별 통계 데이터 없을 때")
    void testGetTeamStatic_withoutData_triggersCreation() {
        // given
        when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(teamMember));
        when(teamStaticsRepository.existsAllByTeamId(TEAM_ID))
                .thenReturn(false);
        when(teamMemberRepository.getUsersByTeamIn(team))
                .thenReturn(List.of(user));

        when(userStaticsRepository.getTeamActivityByWeekdayAndUser(any(), eq(List.of(user))))
                .thenReturn(Optional.empty());

        // when
        List<GeneralStaticsResponse> result = teamStaticsService.getTeamStatic(USER_ID);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("주간 통계 데이터 없을 때")
    void testGetTeamWeekStatics_triggersCreationIfMissing() {
        // given
        when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.of(teamMember));
        when(teamStaticsRepository.existsAllByTeamId(TEAM_ID))
                .thenReturn(false);
        when(teamMemberRepository.getUsersByTeamIn(team))
                .thenReturn(List.of(user));
        when(userStaticsRepository.getTeamActivityByWeekdayAndUser(any(), eq(List.of(user))))
                .thenReturn(Optional.empty());

        // when
        SumStaticResponse result = teamStaticsService.getTeamWeekStatics(USER_ID);

        // then
        assertNotNull(result);
        assertNull(result.getTeams());
        assertNull(result.getDocs());
        assertNull(result.getEmail());
        assertNull(result.getGit());
    }

    @Test
    @DisplayName("평균 통계 없을 때")
    void testGetTeamsAverageStatic_skipOnCreateFailure() {
        // given
        when(teamRepository.findAll())
                .thenReturn(List.of(team));
        when(teamStaticsRepository.existsAllByTeamId(TEAM_ID))
                .thenReturn(false);
        when(teamMemberRepository.getUsersByTeamIn(team))
                .thenReturn(List.of(user));
        when(userStaticsRepository.getTeamActivityByWeekdayAndUser(any(), any()))
                .thenReturn(Optional.empty());

        // when
        List<AverageStaticsResponse> result = teamStaticsService.getTeamsAverageStatic();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("팀원 정보 없을 경우 예외처리")
    void testGetTeamStatic_userNotMappedToTeam_throwsException() {
        // given
        when(teamMemberRepository.findByUserId(USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(TeamMemberNotMappedException.class, () ->
                teamStaticsService.getTeamStatic(USER_ID)
        );
        verify(teamMemberRepository).findByUserId(USER_ID);
    }

    @Test
    @DisplayName("일부 요일 데이터 없을 때 예외처리")
    void testGetUsersAverageStatic_ExceptionHandlingForWeekdays() {
        // given
        when(teamRepository.findAll())
                .thenReturn(List.of(team));
        when(teamStaticsRepository.existsAllByTeamId(TEAM_ID))
                .thenReturn(true);
        // Weekday가 7개니까 7번 호출된다고 가정
        when(teamStaticsRepository.getTeamAvgByDayAndTeam(any(Weekday.class), eq(team)))
                .thenReturn(Optional.of(new AverageActivity()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new AverageActivity()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new AverageActivity()))
                .thenReturn(Optional.empty());

        // when
        List<AverageStaticsResponse> result = teamStaticsService.getTeamsAverageStatic();

        // then
        assertFalse(result.isEmpty());
        verify(teamStaticsRepository, times(Weekday.values().length))
                .getTeamAvgByDayAndTeam(any(Weekday.class), eq(team));
    }
}
