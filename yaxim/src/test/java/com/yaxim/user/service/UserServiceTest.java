package com.yaxim.user.service;

import com.yaxim.git.entity.GitInfo;
import com.yaxim.git.repository.GitInfoRepository;
import com.yaxim.team.entity.Team;
import com.yaxim.team.entity.TeamMember;
import com.yaxim.team.repository.TeamMemberRepository;
import com.yaxim.user.controller.dto.request.UserInfoRequest;
import com.yaxim.user.controller.dto.response.UserInfoResponse;
import com.yaxim.user.entity.UserRole;
import com.yaxim.user.entity.Users;
import com.yaxim.user.exception.UserNotFoundException;
import com.yaxim.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TeamMemberRepository teamMemberRepository;
    @Mock
    private GitInfoRepository gitInfoRepository;

    @InjectMocks
    private UserService userService;

    private static final Long USER_ID = 1L;
    private static final String USER_NAME = "name";
    private static final String USER_EMAIL = "test@example.com";
    private static final String GIT_EMAIL = "git@example.com";
    private static final String TEAM_ID = "team_id";

    private Users user;
    private TeamMember teamMember;
    private GitInfo gitInfo;
    private UserInfoRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new Users(USER_ID, USER_NAME, USER_EMAIL);
        Team team = new Team(
                TEAM_ID,
                "team",
                "description"
        );
        teamMember = new TeamMember(team, user, UserRole.MEMBER);
        gitInfo = new GitInfo(
                1L, user, "test", GIT_EMAIL, "gitURL", "avatarURL"
        );

        updateRequest = new UserInfoRequest("Updated User");
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(gitInfoRepository.findByUser(user)).thenReturn(Optional.of(gitInfo));

        UserInfoResponse response = userService.getUserInfo(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getName()).isEqualTo(USER_NAME);
        assertThat(response.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(response.getGitEmail()).isEqualTo(GIT_EMAIL);
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 사용자 없음")
    void getUserInfo_UserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 정보 조회 실패 - 팀 멤버 없음")
    void getUserInfo_TeamMemberNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 정보 조회 성공 - GitInfo 없음")
    void getUserInfo_Success_NoGitInfo() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(gitInfoRepository.findByUser(user)).thenReturn(Optional.empty());

        UserInfoResponse response = userService.getUserInfo(USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(USER_ID);
        assertThat(response.getGitEmail()).isNull();
    }

    @Test
    @DisplayName("사용자 정보 업데이트 성공")
    void updateUserInfo_Success() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.of(teamMember));
        when(gitInfoRepository.findByUser(user)).thenReturn(Optional.of(gitInfo));

        UserInfoResponse response = userService.updateUserInfo(updateRequest, USER_ID);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Updated User");
        assertThat(user.getName()).isEqualTo("Updated User");
    }

    @Test
    @DisplayName("사용자 정보 업데이트 실패 - 사용자 없음")
    void updateUserInfo_UserNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserInfo(updateRequest, USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("사용자 정보 업데이트 실패 - 팀 멤버 없음")
    void updateUserInfo_TeamMemberNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(teamMemberRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserInfo(updateRequest, USER_ID))
                .isInstanceOf(UserNotFoundException.class);
    }
}
