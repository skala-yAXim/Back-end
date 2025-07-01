package com.yaxim.team.service;

import com.yaxim.global.for_ai.dto.response.TeamWithMemberAndProjectResponse;
import com.yaxim.graph.GraphApiService;
import com.yaxim.graph.controller.dto.GraphTeamMemberResponse;
import com.yaxim.graph.controller.dto.GraphTeamResponse;
import com.yaxim.project.entity.Project;
import com.yaxim.project.entity.ProjectFile;
import com.yaxim.project.repository.ProjectCustomRepository;
import com.yaxim.team.controller.dto.request.WeeklyTemplateRequest;
import com.yaxim.team.controller.dto.response.TeamMemberResponse;
import com.yaxim.team.controller.dto.response.TeamResponse;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.exception.TeamMemberNotMappedException;
import com.yaxim.team.exception.TeamNotFoundException;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.team.repository.TeamRepository;
import com.yaxim.user.entity.Users;
import com.yaxim.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private GraphApiService graphApiService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectCustomRepository projectCustomRepository;

    @InjectMocks
    private TeamService teamService;

    private static final Long USER_ID = 1L;
    private static final String TEAM_ID = "team123";
    private static final String TEAM_NAME = "Test Team";
    private static final String TEAM_DESCRIPTION = "Test Description";

    private Users user;
    private Team team;
    private TeamMember teamMember;
    private WeeklyTemplateRequest templateRequest;
    private GraphTeamResponse.Team graphTeam;
    private List<GraphTeamMemberResponse.Members> graphMembers;
    private List<Project> projects;

    @BeforeEach
    void setUp() {
        user = new Users(USER_ID, "name", "example@test.com");
        team = new Team(TEAM_ID, TEAM_NAME, TEAM_DESCRIPTION);
        teamMember = new TeamMember(team, user);

        templateRequest = new WeeklyTemplateRequest("주간 템플릿");

        graphTeam = new GraphTeamResponse.Team();
        graphTeam.id = TEAM_ID;
        graphTeam.displayName = TEAM_NAME;
        graphTeam.description = TEAM_DESCRIPTION;

        GraphTeamMemberResponse.Members member = new GraphTeamMemberResponse.Members();
        member.email = "test@example.com";
        member.roles = List.of("owner");
        graphMembers = List.of(member);

        Project project = new Project(
                1L,
                "Test Project",
                100,
                team,
                LocalDate.of(2025, 5, 22),
                LocalDate.of(2025,7,6),
                "Test Description",
                List.of(new ProjectFile())
        );

        projects = List.of(project);
    }

    @Test
    @DisplayName("사용자 팀 조회 성공")
    void getUserTeam_Success() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));

        TeamResponse response = teamService.getUserTeam(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(TEAM_NAME);
    }

    @Test
    @DisplayName("사용자 팀 조회 실패 - 팀 멤버 아님")
    void getUserTeam_TeamMemberNotFound() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getUserTeam(USER_ID))
                .isInstanceOf(TeamMemberNotMappedException.class);
    }

    @Test
    @DisplayName("사용자 팀 조회 실패 - 팀 없음")
    void getUserTeam_TeamNotFound() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getUserTeam(USER_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    @DisplayName("주간 템플릿 업데이트 성공")
    void updateTemplate_Success() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));

        teamService.updateTemplate(templateRequest, USER_ID);

        assertThat(team.getWeeklyTemplate()).isEqualTo("주간 템플릿");
    }

    @Test
    @DisplayName("팀 멤버 목록 조회 성공")
    void getUserTeamMembers_Success() {
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(teamMemberRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(teamMember));

        List<TeamMemberResponse> responses = teamService.getUserTeamMembers(USER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo("name");
    }

    @Test
    @DisplayName("팀 로드 성공 - 새 팀 생성")
    void loadTeam_Success_NewTeam() {
        // Given
        when(graphApiService.getMyFirstTeam(USER_ID)).thenReturn(graphTeam);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.empty());
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(graphApiService.getMyTeamMembers(TEAM_ID, USER_ID)).thenReturn(graphMembers);
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(teamMemberRepository.save(any(TeamMember.class))).thenReturn(teamMember);

        // When
        teamService.loadTeam(USER_ID);

        // Then
        verify(teamRepository).save(any(Team.class));
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("팀 로드 성공 - 기존 팀 업데이트")
    void loadTeam_Success_ExistingTeam() {
        when(graphApiService.getMyFirstTeam(USER_ID)).thenReturn(graphTeam);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(graphApiService.getMyTeamMembers(TEAM_ID, USER_ID)).thenReturn(graphMembers);
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));

        teamService.loadTeam(USER_ID);
    }

    @Test
    @DisplayName("팀 로드 실패 - 그래프 팀 없음")
    void loadTeam_TeamNotFound() {
        when(graphApiService.getMyFirstTeam(USER_ID)).thenReturn(null);

        assertThatThrownBy(() -> teamService.loadTeam(USER_ID))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    @DisplayName("모든 팀 정보 조회 성공")
    void getAllTeamsInfo_Success() {
        when(teamRepository.findAll()).thenReturn(List.of(team));
        when(teamMemberRepository.findByTeamId(TEAM_ID)).thenReturn(List.of(teamMember));
        when(projectCustomRepository.findAllInProgress()).thenReturn(projects);

        List<TeamWithMemberAndProjectResponse> responses = teamService.getAllTeamsInfo();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getName()).isEqualTo(TEAM_NAME);
        assertThat(responses.get(0).getMembers()).hasSize(1);
        assertThat(responses.get(0).getProjects()).hasSize(1);
    }
}
